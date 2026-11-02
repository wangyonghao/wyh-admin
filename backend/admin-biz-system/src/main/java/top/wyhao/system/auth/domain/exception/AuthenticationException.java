package top.wyhao.system.auth.domain.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 认证相关业务异常。
 * <p>
 * {@code message}/{@code defaultMessage} 面向前端，凭证类失败应模糊提示；
 * {@code realReason} 写入登录审计日志，供管理员追溯真实原因。
 */
@Setter
@Getter
public class AuthenticationException extends BizException {

    private String realReason;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String code, String message) {
        super(code, message);
    }

    public AuthenticationException(String code, String message, String realReason) {
        super(code, message);
        this.realReason = realReason;
    }

    public static AuthenticationException of(String message) {
        return new AuthenticationException(message);
    }

    public static AuthenticationException of(String code, String message) {
        return new AuthenticationException(code, message);
    }

    public static AuthenticationException of(String code, String message, String realReason) {
        return new AuthenticationException(code, message, realReason);
    }

    /**
     * 密码凭证失败：前端统一模糊文案，真实原因进 realReason。
     */
    public static AuthenticationException passwordError(String realReason) {
        return of("PASSWORD_ERROR", "账号或密码错误", realReason);
    }


    public static AuthenticationException needCaptcha(String realReason) {
        return of("NEED_CAPTCHA", "需要验证码", realReason);
    }

    public static AuthenticationException passwordErrorExceeded(String realReason) {
        return of("TOO_MANY_REQUEST", "请求过于频繁，请稍后再试", realReason);
    }

    public static AuthenticationException captchaRequired() {
        return of("CAPTCHA_IS_REQUIRED", "验证码不能为空");
    }

    public static AuthenticationException captchaInvalid() {
        return of("CAPTCHA_INVALID", "验证码无效");
    }

    public static AuthenticationException captchaExpired() {
        return of("CAPTCHA_EXPIRED", "验证码无效");
    }

    public static AuthenticationException captchaOutdated() {
        return of("AUTH_CAPTCHA_OUTDATED", "验证码已失效");
    }

    public static AuthenticationException captchaIncorrect() {
        return of("AUTH_CAPTCHA_INCORRECT", "验证码不正确");
    }

    /**
     * OTP 凭证失败：前端统一「验证码错误」。
     */
    public static AuthenticationException otpError(String realReason) {
        return of("OTP_ERROR", "验证码错误", realReason);
    }

    public static AuthenticationException phoneNotBound() {
        return of("AUTH_PHONE_NOT_BOUND", "验证码错误", "此手机号未绑定本系统账号");
    }

    public static AuthenticationException emailInvalid() {
        return of("AUTH_EMAIL_INVALID", "邮箱格式不正确");
    }

    public static AuthenticationException phoneInvalid() {
        return of("AUTH_PHONE_INVALID", "手机号格式不正确");
    }

    public static AuthenticationException emailNotBound() {
        return of("AUTH_EMAIL_NOT_BOUND", "验证码错误", "此邮箱未绑定本系统账号");
    }

    public static AuthenticationException socialAuthFailed(String detail) {
        return of("AUTH_SOCIAL_AUTH_FAILED", "登录失败", detail);
    }

    public static AuthenticationException captchaSendFailed() {
        return of("CAPTCHA_SEND_FAILED", "验证码发送失败");
    }

    public static AuthenticationException accountLocked(long minutes) {
        return accountLocked(minutes, null);
    }

    public static AuthenticationException accountLocked(long minutes, String realReason) {
        return of("ACCOUNT_LOCKED", StrUtil.format("账号已锁定, {} 分钟后可重试", minutes), realReason);
    }
    public static AuthenticationException platformNotSupport(String source) {
        return of("PLATFORM_NOT_SUPPORT", "登录失败", StrUtil.format("暂不支持 [{}] 平台账号登录", source));
    }

    public static AuthenticationException kickoutSelfNotAllowed() {
        return of("AUTH_KICKOUT_SELF_NOT_ALLOWED", "不能强退自己");
    }

    public static AuthenticationException accountDisabled() {
        return of("AUTH_ACCOUNT_DISABLED", "此账号已被禁用，如有疑问，请联系管理员");
    }

    public static AuthenticationException accountDepartmentDisabled() {
        return of("AUTH_ACCOUNT_DEPARTMENT_DISABLED", "此账号所属部门已被禁用，如有疑问，请联系管理员");
    }

    public static AuthenticationException registrationDisabled() {
        return of("REGISTRATION_DISABLED", "系统未开放注册");
    }

    public static AuthenticationException passwordMismatch() {
        return of("PASSWORD_MISMATCH", "两次输入的密码不一致");
    }

    public static AuthenticationException registerRoleMissing() {
        return of("REGISTER_ROLE_REQUIRED", "未配置注册默认角色，请联系管理员");
    }

}
