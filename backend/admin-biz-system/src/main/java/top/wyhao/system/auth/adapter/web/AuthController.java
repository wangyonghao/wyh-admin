package top.wyhao.system.auth.adapter.web;

import cn.dev33.satoken.annotation.SaIgnore;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.AuthRequestBuilder;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.web.bind.annotation.*;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.security.client.PermissionApi;
import top.wyhao.system.admin.adapter.client.MenuApi;
import top.wyhao.system.admin.client.UserApi;
import top.wyhao.system.auth.app.dto.*;
import top.wyhao.system.auth.app.service.AuthService;
import top.wyhao.system.auth.domain.exception.AuthenticationException;
import top.wyhao.web.ratelimit.LimitType;
import top.wyhao.web.ratelimit.RateLimiter;
import top.wyhao.web.ratelimit.RateLimiters;

import java.util.concurrent.TimeUnit;

/**
 * 用户认证 API
 */
@Tag(name = "用户认证 API")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JustAuthProperties authProperties;

    private final UserApi userApi;
    private final MenuApi menuApi;
    private final AuthService authService;
    private final PermissionApi permissionProvider;

    @SaIgnore
    @Operation(summary = "登录", description = "用户统一登录入口。grantType 放在 QueryString，Body 为 LoginRequest。")
    @Parameter(name = "grantType", description = "登录方式", required = true,
            example = "PASSWORD", in = ParameterIn.QUERY,
            schema = @Schema(allowableValues = {"PASSWORD", "OTP", "OAUTH"}))
    @PostMapping("/auth/login")
    public AccessTokenResponse login(
            @RequestParam GrantType grantType,
            @RequestBody LoginRequest req) {
        return authService.login(grantType, req);
    }

    @Operation(summary = "登出", description = "注销用户的当前登录")
    @Parameter(name = "Authorization", description = "令牌", required = true, example = "Bearer xxxx-xxxx-xxxx-xxxx", in = ParameterIn.HEADER)
    @PostMapping("/auth/logout")
    public void logout() {
        authService.logout();
    }

    @Operation(summary = "获取图片验证码", description = "获取图片验证码（Base64编码，带图片格式：data:image/gif;base64）")
    @GetMapping("/captcha")
    public CaptchaResponse getImageCaptcha() {
        return authService.createCaptcha();
    }

    @SaIgnore
    @Operation(summary = "注册", description = "用户自助注册并直接登录")
    @PostMapping("/auth/register")
    public AccessTokenResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    private static final String OTP_KEY_PREFIX = "otp:captcha:";

    /**
     * OTP(One-Time Password) 验证码 API
     */
    @Operation(summary = "邮件发送验证码", description = "发送邮件或短信验证码")
    @RateLimiters({
            @RateLimiter(name = OTP_KEY_PREFIX + "MIN", key = "#req.identifier() + ':' + #req.scene()", rate = 2, interval = 1, unit = TimeUnit.MINUTES, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = OTP_KEY_PREFIX + "HOUR", key = "#req.identifier() + ':' + #req.scene()", rate = 8, interval = 1, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = OTP_KEY_PREFIX + "DAY", key = "#req.identifier() + ':' + #req.scene()", rate = 20, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = OTP_KEY_PREFIX, key = "#req.identifier()", rate = 100, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = OTP_KEY_PREFIX, key = "#req.identifier()", rate = 30, interval = 1, unit = TimeUnit.MINUTES, type = LimitType.IP, message = "获取验证码操作太频繁，请稍后再试")})
    @PostMapping("auth/otp")
    public OtpSendResponse sendOTP(@Valid @RequestBody OtpSendRequest req) {
        return authService.sendOTP(req);
    }

    @Operation(summary = "验证验证码", description = "验证用户输入的验证码")
    @PostMapping("auth/otp/verify")
    public OtpVerifyResponse verifyOTP(@Valid @RequestBody OtpVerifyRequest req) {
        return authService.verifyOTP(req);
    }

    @SaIgnore
    @Operation(summary = "三方账号登录授权", description = "三方账号登录授权")
    @Parameter(name = "provider", description = "来源", example = "gitee", in = ParameterIn.PATH)
    @GetMapping("/oauth/{provider}")
    public SocialAuthorizeUrlResult authorize(@PathVariable String source) {
        AuthRequest authRequest = this.getAuthRequest(source);
        return new SocialAuthorizeUrlResult(authRequest.authorize(AuthStateUtils.createState()));
    }

    @Operation(summary = "获取认证信息", description = "获取认证信息")
    @GetMapping("/auth/user")
    public AuthenticationInfoResult getAuthenticationInfo() {
        Long userId = LoginUtil.getUserId();
        return new AuthenticationInfoResult(
                userApi.profile(userId),
                permissionProvider.findUserRoles(userId),
                permissionProvider.findUserPermissions(userId),
                menuApi.getMenuTreeByUserId(userId)
        );
    }

    private AuthRequest getAuthRequest(String source) {
        try {
            AuthConfig authConfig = authProperties.getType().get(source.toUpperCase());
            return AuthRequestBuilder.builder().source(source).authConfig(authConfig).build();
        } catch (Exception e) {
            throw AuthenticationException.platformNotSupport(source);
        }
    }
}
