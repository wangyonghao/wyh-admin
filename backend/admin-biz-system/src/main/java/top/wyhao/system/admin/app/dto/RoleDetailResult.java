package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.cmn.core.enums.DataScopeEnum;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
@Schema(description = "角色详情响应参数")
public class RoleDetailResult {

    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    @JsonIgnore
    private Long createUser;

    @Schema(description = "创建人", example = "超级管理员")
    @ExcelProperty(value = "创建人", order = Integer.MAX_VALUE - 4)
    private String createUserString;

    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "创建时间", order = Integer.MAX_VALUE - 3)
    private LocalDateTime createTime;

    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    @JsonIgnore
    private Long updateUser;

    @Schema(description = "修改人", example = "李四")
    @ExcelProperty(value = "修改人", order = Integer.MAX_VALUE - 2)
    private String updateUserString;

    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "修改时间", order = Integer.MAX_VALUE - 1)
    private LocalDateTime updateTime;

    @Schema(description = "名称", example = "测试人员")
    @ExcelProperty(value = "名称")
    private String name;

    @Schema(description = "编码", example = "test")
    @ExcelProperty(value = "编码")
    private String code;

    @Schema(description = "数据权限", example = "5")
    @ExcelProperty(value = "数据权限", converter = ExcelBaseEnumConverter.class)
    private DataScopeEnum dataScope;

    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序")
    private Integer sort;

    @Schema(description = "是否为系统内置数据", example = "false")
    @ExcelProperty(value = "系统内置")
    private Boolean isBuiltin;

    @Schema(description = "菜单选择是否父子节点关联", example = "false")
    private Boolean menuCheckStrictly;

    @Schema(description = "部门选择是否父子节点关联", example = "false")
    private Boolean departmentCheckStrictly;

    @Schema(description = "描述", example = "测试人员描述信息")
    @ExcelProperty(value = "描述")
    private String description;

    @Schema(description = "功能权限：菜单 ID 列表", example = "1000,1010,1011,1012,1013,1014")
    private List<Long> menuIds;

    @Schema(description = "权限范围：部门 ID 列表", example = "5")
    private List<Long> departmentIds;
}
