package top.wyhao.system.auth.app.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.constant.RegexConstants;
import top.wyhao.cmn.core.enums.GenderEnum;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.system.admin.client.SocialLink;
import top.wyhao.system.auth.app.command.RegisterCmdExe;
import top.wyhao.system.auth.app.dto.*;
import top.wyhao.system.auth.app.service.AuthService;
import top.wyhao.system.auth.domain.exception.AuthenticationException;
import top.wyhao.system.auth.domain.gateway.*;
import top.wyhao.system.auth.domain.model.*;
import top.wyhao.system.auth.domain.otp.Otp;
import top.wyhao.system.auth.domain.otp.OtpDomainService;
import top.wyhao.system.auth.domain.otp.OtpException;
import top.wyhao.system.auth.domain.user.CredentialUser;
import top.wyhao.system.auth.domain.user.PasswordSecurityService;
import top.wyhao.system.auth.domain.user.SocialProfile;
import top.wyhao.web.http.ServletUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RegisterCmdExe registerCmdExe;

    private final UserGateway userGateway;
    private final PasswordSecurityService passwordSecurityService;
    private final OtpDomainService otpDomainService;
    private final PasswordFailGateway passwordFailGateway;
    private final AuthLogGateway authLogGateway;
    private final TokenGateway tokenGateway;
    private final SocialAuthGateway socialAuthGateway;

    // 用户登录
    @Override
    public AccessTokenResponse login(GrantType grantType, LoginRequest req) {
        ClientContext client = clientContext();
        String identifier = resolveIdentifier(grantType, req);

        CredentialUser user;
        try {
            user = switch (grantType) {
                case PASSWORD -> loadUserWithPassword(new PasswordLoginCommand(
                        req.getUsername(),
                        req.getEmail(),
                        req.getPhone(),
                        req.getPassword(),
                        req.getCaptchaUuid(),
                        req.getCaptchaChallenge(),
                        client,
                        passwordFailGateway.currentPolicy()));
                case OTP -> loadUserWithOTP(new OtpLoginCommand(
                        req.getEmail(),
                        req.getPhone(),
                        req.getCodeChallenge(),
                        client));
                case OAUTH -> loadUserWithProvider(new SocialAuthCommand(
                        req.getProvider(),
                        req.getCode(),
                        req.getState(),
                        client));
            };

            // 禁用检查纳入 try，失败同样记审计日志
            user.canLogin();
            if (userGateway.isDepartmentDisabled(user.departmentId())) {
                throw AuthenticationException.accountDepartmentDisabled();
            }
        } catch (AuthenticationException e) {
            authLogGateway.recordFailure(
                    identifier,
                    client.ip(),
                    client.userAgent(),
                    firstNonBlank(e.getRealReason(), e.getMessage()));
            throw e;
        } catch (Exception e) {
            authLogGateway.recordFailure(
                    identifier,
                    client.ip(),
                    client.userAgent(),
                    "系统异常: " + e.getMessage());
            throw e;
        }

        String token = tokenGateway.issue(user, client);
        authLogGateway.recordSuccess(user.username(), client.ip(), client.userAgent());
        // 密码过期仅站内提醒，不阻断登录、不发临时 token
        try {
            userGateway.notifyPasswordExpiryReminder(user);
        } catch (Exception ignored) {
            // 提醒失败不影响登录
        }
        return new AccessTokenResponse("200", token);
    }

    private CredentialUser loadUserWithPassword(PasswordLoginCommand command) {
        ClientContext client = command.client();
        String ip = client != null ? client.ip() : null;
        String identifier = command.identifier();

        passwordSecurityService.assertNotBanned(identifier, ip);
        passwordSecurityService.verifyCaptchaIfNeeded(identifier, command.captchaUuid(), command.captcha());
        passwordSecurityService.fastVerifyPasswordFormat(identifier, command.password(), client);

        CredentialUser user = findPasswordUser(command);
        if (user == null) {
            // 用户不存在也计入失败，防止扫号绕过图验/锁定；rejectCredential 必定抛异常
            passwordSecurityService.rejectCredential(identifier, client, "用户不存在：" + identifier);
            throw AuthenticationException.passwordError("用户不存在：" + identifier);
        }

        passwordSecurityService.verifyPassword(user, command.password(), client);
        return user;
    }

    private CredentialUser findPasswordUser(PasswordLoginCommand command) {
        if (StrUtil.isNotBlank(command.username())) {
            return userGateway.findByUsername(command.username());
        }
        if (StrUtil.isNotBlank(command.email())) {
            return userGateway.findByEmail(command.email());
        }
        if (StrUtil.isNotBlank(command.phone())) {
            return userGateway.findByPhone(command.phone());
        }
        return null;
    }

    private CredentialUser loadUserWithOTP(@Valid OtpLoginCommand command) {
        String contact = StrUtil.isNotBlank(command.email()) ? command.email() : command.phone();
        try {
            // 与发码 / verifyOTP / 改邮改手机同一套：比对后一次性消费
            otpDomainService.verify(contact, command.codeChallenge(), null);
        } catch (OtpException e) {
            throw AuthenticationException.otpError(e.getMessage() + "：" + contact);
        }
        return resolveContactUser(command);
    }

    private CredentialUser loadUserWithProvider(SocialAuthCommand command) {
        tokenGateway.logoutIfLoggedIn();
        SocialProfile profile = socialAuthGateway.authenticate(
                command.provider(), command.code(), command.state());

        SocialLink link = userGateway.findSocial(profile.source(), profile.openId());
        CredentialUser user;
        if (link == null) {
            user = provisionNewUser(profile);
        } else {
            user = userGateway.findById(link.userId());
            if (user == null) {
                throw AuthenticationException.socialAuthFailed(
                        "社交账号已绑定但用户不存在：source=" + profile.source()
                                + ", openId=" + profile.openId()
                                + ", userId=" + link.userId());
            }
        }
        userGateway.saveSocialLogin(user.id(), profile);
        return user;
    }

    @Override
    public AccessTokenResponse register(@Valid RegisterRequest request) {
        return registerCmdExe.execute(request);
    }

    @Override
    public void logout() {
        try {
            LoginUtil.logout();
        } catch (NotLoginException ignored) {
        }
    }

    @Override
    public CaptchaResponse createCaptcha() {
        var captcha = passwordSecurityService.createCaptcha();
        return new CaptchaResponse(
                captcha.uuid(),
                captcha.img(),
                captcha.expireTime(),
                captcha.alwaysRequired());
    }

    @Override
    public OtpSendResponse sendOTP(@Valid OtpSendRequest req) {
        // 只生成一次 code：Redis 与邮件/短信共用
        Otp otp = otpDomainService.issue(req.identifier());
        String clientIp = ServletUtils.getRequestIp();

        if (StrUtil.isNotBlank(req.email())) {
            otpDomainService.sendEmail(req.email(), req.scene(), req.locale(), otp, clientIp);
        } else {
            otpDomainService.sendSms(req.phone(), req.scene(), otp);
        }
        return new OtpSendResponse(otp.expireSeconds(), "验证码已发送", otp.uuid());
    }

    @Override
    public OtpVerifyResponse verifyOTP(OtpVerifyRequest req) {
        String contact = StrUtil.isNotBlank(req.email()) ? req.email() : req.phone();
        otpDomainService.verify(contact, req.code(), req.otpUuid());
        return new OtpVerifyResponse(true, "验证成功");
    }

    private CredentialUser resolveContactUser(OtpLoginCommand command) {
        if (StrUtil.isNotBlank(command.email())) {
            CredentialUser user = userGateway.findByEmail(command.email());
            if (user == null) {
                throw AuthenticationException.emailNotBound();
            }
            return user;
        }
        CredentialUser user = userGateway.findByPhone(command.phone());
        if (user == null) {
            throw AuthenticationException.phoneNotBound();
        }
        return user;
    }

    private CredentialUser provisionNewUser(SocialProfile profile) {
        String randomStr = RandomUtil.randomString(RandomUtil.BASE_CHAR, 5);
        String usernameFallback = randomStr + IdUtil.fastSimpleUUID();
        String displayFallback = profile.source().toLowerCase() + randomStr;
        CredentialUser existsUser = userGateway.findByUsername(profile.username());
        String username = resolveUsername(
                profile.username(), existsUser != null, usernameFallback);
        String displayName = resolveDisplayName(profile.nickname(), displayFallback);
        Integer gender = null;
        if (profile.genderCode() != null) {
            gender = GenderEnum.getByValue(Integer.parseInt(profile.genderCode())).getValue();
        }
        CredentialUser user = userGateway.registerSocialUser(username, displayName, gender);
        userGateway.assignGeneralUserRole(user.id());
        userGateway.notifyWelcome(user);
        return user;
    }

    public String resolveDisplayName(String preferred, String fallback) {
        if (preferred == null || !ReUtil.isMatch(RegexConstants.GENERAL_NAME, preferred)) {
            return fallback;
        }
        return preferred;
    }

    public String resolveUsername(String preferred, boolean usernameTaken, String fallback) {
        if (usernameTaken || preferred == null || !ReUtil.isMatch(RegexConstants.USERNAME, preferred)) {
            return fallback;
        }
        return preferred;
    }

    private static String resolveIdentifier(GrantType grantType, LoginRequest req) {
        return switch (grantType) {
            case PASSWORD -> firstNonBlank(req.getUsername(), req.getEmail(), req.getPhone(), "unknown");
            case OTP -> firstNonBlank(req.getEmail(), req.getPhone(), "unknown");
            case OAUTH -> firstNonBlank(req.getProvider(), "oauth");
        };
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static ClientContext clientContext() {
        String ip = ServletUtils.getRequestIp();
        HttpServletRequest http = ServletUtils.getRequest();
        String ua = http != null ? http.getHeader("User-Agent") : null;
        return ClientContext.of(ip, ua);
    }

}
