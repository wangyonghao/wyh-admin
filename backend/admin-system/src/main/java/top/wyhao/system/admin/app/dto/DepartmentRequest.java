package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.wyhao.web.validation.EnumValue;

/**
 * 部门创建或修改请求参数
 */
@Data
@Schema(description = "部门创建或修改请求参数")
public class DepartmentRequest {

    @Schema(description = "编码", example = "A001")
    @NotBlank(message = "编码不能为空")
    @Length(max = 30, message = "编码长度不能超过 {max} 个字符")
    private String code;

    @Schema(description = "名称", example = "测试部")
    @NotBlank(message = "名称不能为空")
    @Length(max = 50, message = "名称长度不能超过 {max} 个字符")
    private String name;

    @NotNull(message = "部门类型不能为空")
    private Integer type;

    @Schema(description = "上级部门 ID", example = "2")
    @NotNull(message = "上级部门不能为空")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    @Min(value = 1, message = "排序最小值为 {value}")
    private Integer sort;

    @Schema(description = "描述", example = "测试部描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    @Schema(description = "状态", example = "1")
    @EnumValue(enumValues = {"1", "2"}, message = "状态值必须是 1 或 2")
    private Integer status;
}
