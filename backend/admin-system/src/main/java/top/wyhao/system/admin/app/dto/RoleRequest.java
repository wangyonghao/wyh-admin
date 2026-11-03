package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.wyhao.cmn.core.constant.RegexConstants;
import top.wyhao.cmn.core.enums.DataScopeEnum;

import java.util.List;

/**
 * 角色创建或修改请求参数
 */
@Data
@Schema(description = "角色创建或修改请求参数")
public class RoleRequest {

    @Schema(description = "名称", example = "测试人员")
    @NotBlank(message = "名称不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_NAME, message = "名称长度为 2-30 个字符，支持中文、字母、数字、下划线，短横线")
    private String name;

    @Schema(description = "编码", example = "test")
    @NotBlank(message = "编码不能为空")
    @Pattern(regexp = RegexConstants.GENERAL_CODE, message = "编码长度为 2-30 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String code;

    @Schema(description = "排序", example = "1")
    @Min(value = 0, message = "排序最小值为 {value}")
    private Integer sort;

    @Schema(description = "描述", example = "测试人员描述信息")
    @Length(max = 200, message = "描述长度不能超过 {max} 个字符")
    private String description;

    @Schema(description = "数据权限", example = "5")
    private DataScopeEnum dataScope;

    @Schema(description = "权限范围：部门 ID 列表", example = "5")
    private List<Long> departmentIds;

    @Schema(description = "部门选择是否父子节点关联", example = "false")
    private Boolean departmentCheckStrictly;
}
