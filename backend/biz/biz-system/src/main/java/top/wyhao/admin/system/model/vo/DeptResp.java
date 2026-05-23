
package top.wyhao.admin.system.model.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;

import java.util.List;

/**
 * 部门响应参数
 *

 * @since 2023/1/22 13:53
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "部门响应参数")
public class DeptResp {
    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "测试部")
    @ExcelProperty(value = "名称", order = 2)
    private String name;

    /**
     * 上级部门 ID
     */
    @Schema(description = "上级部门 ID", example = "2")
    @ExcelProperty(value = "上级部门 ID", order = 3)
    private Long parentId;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序", order = 6)
    private Integer sort;

    /**
     * 是否为系统内置数据
     */
    @Schema(description = "是否为系统内置数据", example = "false")
    @ExcelProperty(value = "系统内置", order = 7)
    private Boolean isBuiltin;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "测试部描述信息")
    @ExcelProperty(value = "描述", order = 8)
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = ExcelBaseEnumConverter.class, order = 5)
    private StatusEnum status;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    @Schema(description = "下级部门", example = "true")
    private List<DeptResp> children;

}