package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 选项响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "选项响应参数")
public class OptionResult {

    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID")
    private Long id;

    @Schema(description = "选项类型", example = "notice_type")
    @ExcelProperty(value = "选项类型")
    private String optionType;

    @Schema(description = "选项值", example = "1")
    @ExcelProperty(value = "选项值")
    private String value;

    @Schema(description = "选项标签", example = "产品新闻")
    @ExcelProperty(value = "选项标签")
    private String label;

    @Schema(description = "扩展信息")
    private Map<String, Object> ext;

    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序")
    private Integer sort;

    @Schema(description = "是否启用", example = "true")
    @ExcelProperty(value = "是否启用")
    private Boolean enabled;

    @Schema(description = "描述")
    @ExcelProperty(value = "描述")
    private String description;
}
