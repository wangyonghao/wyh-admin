package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统配置请求信息
 */
@Data
@Schema(description = "系统配置请求信息")
public class ConfigRequest {

    @Schema(description = "配置键", example = "site")
    @NotBlank(message = "配置键不能为空", groups = {Create.class})
    @Size(max = 100, message = "配置键长度不能超过 100 个字符", groups = {Create.class, Update.class})
    private String configKey;

    @Schema(description = "配置值（JSON格式）", example = "{\"siteName\":\"WYH Admin\"}")
    private String configValue;

    @Schema(description = "配置说明", example = "站点配置")
    @Size(max = 255, message = "配置说明长度不能超过 255 个字符", groups = {Create.class, Update.class})
    private String description;

    @Schema(description = "版本号", example = "1")
    private Integer version;

    public interface Create {}

    public interface Update {}
}
