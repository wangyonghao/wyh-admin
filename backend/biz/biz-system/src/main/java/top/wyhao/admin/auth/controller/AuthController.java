
package top.wyhao.admin.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.AuthRequestBuilder;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.auth.handler.AccountLoginHandler;
import top.wyhao.admin.auth.handler.EmailLoginHandler;
import top.wyhao.admin.auth.handler.PhoneLoginHandler;
import top.wyhao.admin.auth.handler.SocialLoginHandler;
import top.wyhao.admin.auth.model.*;
import top.wyhao.admin.auth.model.enums.AuthType;
import top.wyhao.admin.system.model.bo.user.UserPasswordResetRequest;
import top.wyhao.admin.system.service.MenuService;
import top.wyhao.admin.system.service.UserService;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.exception.BadRequestException;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.util.RsaUtils;
import top.wyhao.starter.core.util.validation.Validator;

import java.util.Map;

/**
 * 认证 API

 */
@Tag(name = "认证 API")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JustAuthProperties authProperties;

    private final UserService userService;
    private final MenuService menuService;

    @SaIgnore
    @Operation(summary = "登录", description = "用户登录")
    @PostMapping("/auth/login")
    public LoginResult login(@RequestBody @Valid LoginRequest req) {
        if (req.getAuthType() == null) {
            req.setAuthType(AuthType.ACCOUNT);
        }
        LoginResult loginResult;
        switch (req.getAuthType()) {
            case ACCOUNT: // 账号密码登录
                AccountLoginRequest accountReq = (AccountLoginRequest) req;
                Validator.validate(accountReq);
                AccountLoginHandler accountLoginHandler = SpringUtil.getBean(AccountLoginHandler.class);
                loginResult = accountLoginHandler.login(accountReq);
                break;
            case SOCIAL: // 社交账号登录
                SocialLoginRequest socialReq = (SocialLoginRequest) req;
                Validator.validate(socialReq);
                SocialLoginHandler socialLoginHandler = SpringUtil.getBean(SocialLoginHandler.class);
                loginResult = socialLoginHandler.login(socialReq);
                break;
            case EMAIL: // 邮箱登录
                EmailLoginRequest emailReq = (EmailLoginRequest) req;
                Validator.validate(emailReq);
                EmailLoginHandler emailLoginHandler = SpringUtil.getBean(EmailLoginHandler.class);
                loginResult = emailLoginHandler.login(emailReq);
                break;
            case PHONE:  // 手机登录
                PhoneLoginRequest phoneReq = (PhoneLoginRequest) req;
                Validator.validate(phoneReq);
                PhoneLoginHandler phoneLoginHandler = SpringUtil.getBean(PhoneLoginHandler.class);
                loginResult = phoneLoginHandler.login(phoneReq);
                break;
            default:
                throw new BadRequestException("AUTH_TYPE_INVALID", "认证类型无效");
        }
        return loginResult;

    }

    @SaIgnore
    @Operation(summary = "(密码过期时）强制用户修改密码", description = "通过临时令牌修改密码")
    @PostMapping("/auth/force-change-password")
    public void forceChangePassword(@RequestBody Map<String, String> body) {
        String tempToken = body.getOrDefault("tempToken", body.get("temp-token"));
        String newPasswordEnc = body.get("newPassword");
        Object userIdObj = SaTempUtil.parseToken(tempToken);
        if (userIdObj == null) {
            throw new BusinessException("TEMPTOKEN_EXPIRED", "临时令牌无效或已过期");
        }
        String newPassword = RsaUtils.decryptPasswordByRsaPrivateKey(newPasswordEnc, "新密码解密失败");
        UserPasswordResetRequest resetReq = new UserPasswordResetRequest();
        resetReq.setNewPassword(newPassword);
        userService.resetPassword(resetReq, Convert.toLong(userIdObj));
    }

    @Operation(summary = "登出", description = "注销用户的当前登录")
    @Parameter(name = "Authorization", description = "令牌", required = true, example = "Bearer xxxx-xxxx-xxxx-xxxx", in = ParameterIn.HEADER)
    @PostMapping("/auth/logout")
    public void logout() {
        try {
            LoginUtil.logout();
        } catch (NotLoginException ignored) {
        }
    }

    @SaIgnore
    @Operation(summary = "三方账号登录授权", description = "三方账号登录授权")
    @Parameter(name = "source", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @GetMapping("/auth/{source}")
    public SocialAuthorizeUrlResult socialLogin(@PathVariable String source) {
        AuthRequest authRequest = this.getAuthRequest(source);
        return SocialAuthorizeUrlResult.builder()
                .authorizeUrl(authRequest.authorize(AuthStateUtils.createState()))
                .build();
    }

    @Operation(summary = "获取认证信息", description = "获取认证信息")
    @GetMapping("/auth/info")
    public AuthInfoResult getAuthInfo() {
        Long userId = LoginUtil.getUserId();
        AuthInfoResult authInfoResult = new AuthInfoResult();
        authInfoResult.setUser(userService.detail(userId));
        authInfoResult.setRoles(userService.findUserRoles(userId));
        authInfoResult.setPermissions(userService.findUserPermissions(userId));
        authInfoResult.setMenus(menuService.getMenuTreeByUserId(userId));
        return authInfoResult;
    }

    private AuthRequest getAuthRequest(String source) {
        try {
            AuthConfig authConfig = authProperties.getType().get(source.toUpperCase());
            return AuthRequestBuilder.builder().source(source).authConfig(authConfig).build();
        } catch (Exception e) {
            throw new BusinessException("PLATFORM_NOT_SUPPORT", "暂不支持 [%s] 平台账号登录".formatted(source));
        }
    }
}
