package top.wyhao.system.admin.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置响应信息
 *
 * @since 2024/4/26
 */
@Data
@Schema(description = "系统配置响应信息")
public class ConfigResult {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "配置键", example = "site")
    private String configKey;

    @Schema(description = "配置值（JSON格式）", example = "{\"siteName\":\"WYH Admin\"}")
    private String configValue;

    @Schema(description = "配置说明", example = "站点配置")
    private String description;

    @Schema(description = "版本号", example = "1")
    @JsonIgnore
    private Integer version;

    @Schema(description = "创建时间", example = "2024-04-26 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-04-26 10:00:00")
    private LocalDateTime updatedAt;
}
