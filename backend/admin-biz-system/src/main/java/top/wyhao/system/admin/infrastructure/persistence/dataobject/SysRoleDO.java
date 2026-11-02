
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.cmn.core.enums.DataScopeEnum;
import top.wyhao.tenant.core.TenantedEntity;

import java.time.LocalDateTime;

/**
 * 角色实体
 *

 * @since 2023/2/8 22:54
 */
@Data
@TableName("sys_role")
public class SysRoleDO implements TenantedEntity {

    @TableId
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 数据权限
     */
    private DataScopeEnum dataScope;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否为系统内置数据
     */
    private Boolean isBuiltin;

    /**
     * 菜单选择是否父子节点关联
     */
    private Boolean menuCheckStrictly;

    /**
     * 部门选择是否父子节点关联
     */
    private Boolean departmentCheckStrictly;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateUser;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 租户 ID。行级隔离列，与当前登录租户一致。
     */
    private Long tenantId;
}
