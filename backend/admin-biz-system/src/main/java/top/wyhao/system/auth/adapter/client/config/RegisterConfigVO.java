package top.wyhao.system.auth.adapter.client.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 注册相关配置。
 */
@Data
@Schema(description = "注册配置")
public class RegisterConfigVO {

    @Schema(description = "是否开启注册", example = "true")
    private Boolean enabled;

    @Schema(description = "注册是否需要邮箱验证", example = "false")
    private Boolean verifyEmail;

    @Schema(description = "注册是否需要手机验证", example = "false")
    private Boolean verifyPhone;

    @Schema(description = "注册默认角色 ID；为空时回落 general", example = "3")
    private String defaultRoleId;

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
        return this;
    }
}
