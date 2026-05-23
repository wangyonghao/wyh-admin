
package top.wyhao.admin.system.model.vo.role;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.starter.core.enums.DataScopeEnum;
import top.wyhao.starter.excel.converter.ExcelBaseEnumConverter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情响应参数
 *

 * @since 2023/2/1 22:19
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "角色详情响应参数")
public class RoleDetailResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    /**
     * 创建人
     */
    @JsonIgnore
    private Long createUser;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "超级管理员")
    @ExcelProperty(value = "创建人", order = Integer.MAX_VALUE - 4)
    private String createUserString;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "创建时间", order = Integer.MAX_VALUE - 3)
    private LocalDateTime createTime;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    /**
     * 修改人
     */
    @JsonIgnore
    private Long updateUser;

    /**
     * 修改人
     */
    @Schema(description = "修改人", example = "李四")
    @ExcelProperty(value = "修改人", order = Integer.MAX_VALUE - 2)
    private String updateUserString;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "修改时间", order = Integer.MAX_VALUE - 1)
    private LocalDateTime updateTime;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "测试人员")
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * 编码
     */
    @Schema(description = "编码", example = "test")
    @ExcelProperty(value = "编码")
    private String code;

    /**
     * 数据权限
     */
    @Schema(description = "数据权限", example = "5")
    @ExcelProperty(value = "数据权限", converter = ExcelBaseEnumConverter.class)
    private DataScopeEnum dataScope;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    @ExcelProperty(value = "排序")
    private Integer sort;

    /**
     * 是否为系统内置数据
     */
    @Schema(description = "是否为系统内置数据", example = "false")
    @ExcelProperty(value = "系统内置")
    private Boolean isBuiltin;

    /**
     * 菜单选择是否父子节点关联
     */
    @Schema(description = "菜单选择是否父子节点关联", example = "false")
    private Boolean menuCheckStrictly;

    /**
     * 部门选择是否父子节点关联
     */
    @Schema(description = "部门选择是否父子节点关联", example = "false")
    private Boolean deptCheckStrictly;

    /**
     * 描述
     */
    @Schema(description = "描述", example = "测试人员描述信息")
    @ExcelProperty(value = "描述")
    private String description;

    /**
     * 功能权限：菜单 ID 列表
     */
    @Schema(description = "功能权限：菜单 ID 列表", example = "1000,1010,1011,1012,1013,1014")
    private List<Long> menuIds;

    /**
     * 权限范围：部门 ID 列表
     */
    @Schema(description = "权限范围：部门 ID 列表", example = "5")
    private List<Long> deptIds;

    public Boolean getDisabled() {
        return this.getIsBuiltin();
    }
}