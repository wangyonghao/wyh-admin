
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色和部门关联实体
 *

 * @since 2023/2/18 21:57
 */
@Data
@NoArgsConstructor
@TableName("sys_role_department")
public class SysRoleDepartmentDO {

    @TableId
    private Long id;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 部门 ID
     */
    private Long departmentId;

    public SysRoleDepartmentDO(Long roleId, Long departmentId) {
        this.roleId = roleId;
        this.departmentId = departmentId;
    }
}
