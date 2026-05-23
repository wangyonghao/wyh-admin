
package top.wyhao.admin.system.model.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统配置请求信息
 *

 * @since 2024/04/26
 */
@Data
@Schema(description = "系统配置请求信息")
public class ConfigRequest {


    /**
     * 配置键
     */
    @Schema(description = "配置键", example = "site")
    @NotBlank(message = "配置键不能为空", groups = {Create.class})
    @Size(max = 100, message = "配置键长度不能超过 100 个字符", groups = {Create.class, Update.class})
    private String configKey;

    /**
     * 配置值（JSON格式）
     */
    @Schema(description = "配置值（JSON格式）", example = "{\"siteName\":\"WYH Admin\"}")
    private String configValue;

    /**
     * 配置说明
     */
    @Schema(description = "配置说明", example = "站点配置")
    @Size(max = 255, message = "配置说明长度不能超过 255 个字符", groups = {Create.class, Update.class})
    private String description;

    /**
     * 乐观锁版本号
     */
    @Schema(description = "版本号", example = "1")
    private Integer version;

    /**
     * 创建校验组
     */
    public interface Create {
    }

    /**
     * 更新校验组
     */
    public interface Update {
    }
}
