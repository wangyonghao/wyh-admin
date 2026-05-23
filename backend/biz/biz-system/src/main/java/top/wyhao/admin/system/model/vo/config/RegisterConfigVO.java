
package top.wyhao.admin.system.model.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 注册配置
 *

 * @since 2024/04/26
 */
@Data
@Schema(description = "注册配置")
public class RegisterConfigVO {

    /**
     * 是否开启注册
     */
    @Schema(description = "是否开启注册", example = "true")
    private Boolean enabled;

    /**
     * 注册是否需要邮箱验证
     */
    @Schema(description = "注册是否需要邮箱验证", example = "false")
    private Boolean verifyEmail;

    /**
     * 注册是否需要手机验证
     */
    @Schema(description = "注册是否需要手机验证", example = "false")
    private Boolean verifyPhone;

    /**
     * 注册默认角色ID
     */
    @Schema(description = "注册默认角色ID", example = "")
    private String defaultRoleId;
}
