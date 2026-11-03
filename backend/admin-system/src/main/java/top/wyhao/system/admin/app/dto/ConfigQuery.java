package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统配置查询条件
 */
@Data
@Schema(description = "系统配置查询条件")
public class ConfigQuery {

    @Schema(description = "配置键", example = "site")
    private String configKey;

    @Schema(description = "关键词", example = "站点")
    private String searchWords;

    @Schema(description = "排序条件", example = "createTime,desc")
    private String[] sort;
}
