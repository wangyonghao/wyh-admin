package top.wyhao.system.admin.domain.exception;

import top.wyhao.cmn.core.exception.BizException;

/**
 * 系统配置业务异常
 */
public class ConfigException extends BizException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String code, String message) {
        super(code, message);
    }

    public static ConfigException of(String message) {
        return new ConfigException(message);
    }

    public static ConfigException of(String code, String message) {
        return new ConfigException(code, message);
    }

    public static ConfigException mailTestFailed(String detail) {
        return of("MAIL_TEST_FAILED", "测试邮件发送失败：" + detail);
    }

    public static ConfigException notFound() {
        return of("CONFIG_NOT_FOUND", "配置不存在");
    }

    public static ConfigException keyExists() {
        return of("CONFIG_KEY_EXISTS", "配置键已存在");
    }

    public static ConfigException updateConflict() {
        return of("CONFIG_UPDATE_CONFLICT", "更新失败，配置可能已被修改，请刷新后重试");
    }

    public static ConfigException updateFailed() {
        return of("CONFIG_UPDATE_FAILED", "更新配置失败");
    }

    public static ConfigException mailConfigNotFound() {
        return of("CONFIG_MAIL_NOT_FOUND", "邮件配置不存在");
    }

    public static ConfigException mailHostNotConfigured() {
        return of("CONFIG_MAIL_HOST_NOT_CONFIGURED", "邮件服务器地址未配置");
    }

    public static ConfigException mailUsernameNotConfigured() {
        return of("CONFIG_MAIL_USERNAME_NOT_CONFIGURED", "发件人邮箱未配置");
    }

    public static ConfigException mailPasswordNotConfigured() {
        return of("CONFIG_MAIL_PASSWORD_NOT_CONFIGURED", "邮箱密码未配置");
    }

    public static ConfigException mailTestUserNotLoggedIn() {
        return of("CONFIG_MAIL_TEST_USER_NOT_LOGGED_IN", "用户未登录");
    }

    public static ConfigException mailTestUserNotFound() {
        return of("CONFIG_MAIL_TEST_USER_NOT_FOUND", "用户信息不存在");
    }

    public static ConfigException mailTestUserEmailBlank() {
        return of("CONFIG_MAIL_TEST_USER_EMAIL_BLANK", "用户邮箱为空，请先设置邮箱地址");
    }

    public static ConfigException passwordPolicyInvalid(String message) {
        return of("CONFIG_PASSWORD_POLICY_INVALID", message);
    }

    public static ConfigException passwordWarningDaysExceedExpiration() {
        return of("CONFIG_PASSWORD_WARNING_DAYS_EXCEED_EXPIRATION", "密码到期提醒时间应小于密码有效期");
    }
}
