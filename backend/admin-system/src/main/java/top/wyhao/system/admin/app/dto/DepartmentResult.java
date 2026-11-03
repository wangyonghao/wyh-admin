package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 部门响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
@Schema(description = "部门响应参数")
public class DepartmentResult {

    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    @Schema(description = "名称", example = "测试部")
    @ExcelProperty(value = "名称", order = 2)
    private String name;

    @Schema(description = "编码", example = "A01")
    @ExcelProperty(value = "编码", order = 3)
    private String code;

    @Schema(description = "类型", example = "1-分公司，2-部门，3-用户组")
    @ExcelProperty(value = "类型(1-分公司，2-部门，3-用户组)", order = 4)
    private String type;

    @Schema(description = "上级部门 ID", example = "2")
    @ExcelProperty(value = "上级部门 ID", order = 4)
    private Long parentId;

    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序", order = 4)
    private Integer sort;

    @Schema(description = "是否为系统内置数据", example = "false")
    @ExcelProperty(value = "系统内置", order = 6)
    private Boolean isBuiltin;

    @Schema(description = "描述", example = "测试部描述信息")
    @ExcelProperty(value = "描述", order = 7)
    private String description;

    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", order = 5)
    private Integer status;

    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    @Schema(description = "下级部门", example = "true")
    private List<DepartmentResult> children;
}
