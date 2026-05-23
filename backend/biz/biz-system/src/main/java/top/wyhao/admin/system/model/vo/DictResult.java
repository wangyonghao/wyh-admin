
package top.wyhao.admin.system.model.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 字典响应参数
 *

 * @since 2026/5/13
 */
@Data
@Schema(description = "字典响应参数")
public class DictResult {
    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID")
    private Long id;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型", example = "notice_type")
    @ExcelProperty(value = "字典类型")
    private String dictType;

    /**
     * 字典值
     */
    @Schema(description = "字典值", example = "1")
    @ExcelProperty(value = "字典值")
    private String value;

    /**
     * 字典标签
     */
    @Schema(description = "字典标签", example = "产品新闻")
    @ExcelProperty(value = "字典标签")
    private String label;

    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    private Map<String, Object> ext;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序")
    private Integer sort;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    @ExcelProperty(value = "是否启用")
    private Boolean enabled;

    /**
     * 描述
     */
    @Schema(description = "描述")
    @ExcelProperty(value = "描述")
    private String description;
}