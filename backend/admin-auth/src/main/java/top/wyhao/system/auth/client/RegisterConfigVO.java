package top.wyhao.system.auth.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 注册相关配置。
 */
@Data
@Schema(description = "注册配置")
public class RegisterConfigVO {

    public static final int DEFAULT_VERIFICATION_TTL_MINUTES = 30;

    public static final int DEFAULT_RESEND_INTERVAL_SECONDS = 60;

    @Schema(description = "是否开启注册", example = "true")
    private Boolean enabled;

    @Schema(description = "注册是否需要邮箱验证", example = "false")
    private Boolean verifyEmail;

    @Schema(description = "注册是否需要手机验证", example = "false")
    private Boolean verifyPhone;

    @Schema(description = "注册默认角色 ID；为空时回落 general", example = "3")
    private String defaultRoleId;

    @Schema(description = "邮箱验证链接有效分钟数。缺省或小于 5 时按 30，最大 1440。", example = "30")
    private Integer verificationTtlMinutes;

    @Schema(description = "验证邮件重发间隔秒数。缺省或小于 30 时按 60，最大 3600。", example = "60")
    private Integer resendIntervalSeconds;

    public boolean isRegistrationEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public RegisterConfigVO normalizeForRead() {
        if (enabled == null) {
            enabled = false;
        }
        if (verifyEmail == null) {
            verifyEmail = false;
        }
        if (verifyPhone == null) {
            verifyPhone = false;
        }
        if (defaultRoleId == null) {
            defaultRoleId = "";
        }
        verificationTtlMinutes = resolvedTtlMinutes();
        resendIntervalSeconds = resolvedResendIntervalSeconds();
        return this;
    }

    public int resolvedTtlMinutes() {
        if (verificationTtlMinutes == null) {
            return DEFAULT_VERIFICATION_TTL_MINUTES;
        }
        return Math.min(1440, Math.max(5, verificationTtlMinutes));
    }

    public int resolvedResendIntervalSeconds() {
        if (resendIntervalSeconds == null) {
            return DEFAULT_RESEND_INTERVAL_SECONDS;
        }
        return Math.min(3600, Math.max(30, resendIntervalSeconds));
    }
}
