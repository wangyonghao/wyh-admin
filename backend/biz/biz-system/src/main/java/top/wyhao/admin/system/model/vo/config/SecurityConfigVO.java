
package top.wyhao.admin.system.model.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 安全配置
 *

 * @since 2024/04/26
 */
@Data
@Schema(description = "安全配置")
public class SecurityConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 密码最小长度
     */
    @Schema(description = "密码最小长度", example = "8")
    private Integer passwordMinLength;

    /**
     * 密码是否需要大写字母
     */
    @Schema(description = "密码是否需要大写字母", example = "true")
    private Boolean passwordRequireUppercase;

    /**
     * 密码是否需要小写字母
     */
    @Schema(description = "密码是否需要小写字母", example = "true")
    private Boolean passwordRequireLowercase;

    /**
     * 密码是否需要数字
     */
    @Schema(description = "密码是否需要数字", example = "true")
    private Boolean passwordRequireNumber;

    /**
     * 密码是否需要特殊字符
     */
    @Schema(description = "密码是否需要特殊字符", example = "false")
    private Boolean passwordRequireSpecial;

    /**
     * 会话超时时间（分钟）
     */
    @Schema(description = "会话超时时间（分钟）", example = "30")
    private Integer sessionTimeout;

    @Schema(description = "密码过期天数", example = "90")
    private Integer passwordExpireDays = 90;

    private Boolean passwordAllowContainUsername = false;

    private Integer passwordRepetitionTimes = 3;
}
