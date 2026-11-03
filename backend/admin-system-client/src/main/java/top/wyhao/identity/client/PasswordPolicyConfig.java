package top.wyhao.identity.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 密码策略配置（持久化 key：{@code password-policy}）。
 */
@Data
@Schema(description = "密码策略配置")
public class PasswordPolicyConfig {

    @Schema(description = "密码最小长度", example = "8")
    private Integer passwordMinLength;

    @Schema(description = "密码是否需要大写字母", example = "true")
    private Boolean passwordRequireUppercase;

    @Schema(description = "密码是否需要小写字母", example = "true")
    private Boolean passwordRequireLowercase;

    @Schema(description = "密码是否需要数字", example = "true")
    private Boolean passwordRequireNumber;

    @Schema(description = "密码是否需要特殊字符", example = "false")
    private Boolean passwordRequireSpecial;

    @Schema(description = "密码过期天数", example = "90")
    private Integer passwordExpireDays = 90;

    @Schema(description = "是否允许密码包含用户名", example = "false")
    private Boolean passwordAllowContainUsername = false;

    @Schema(description = "历史密码重复校验次数（3~10）", example = "3")
    private Integer passwordRepetitionTimes = 3;
}
