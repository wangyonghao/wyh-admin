
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色和部门关联实体
 *
 * @author Charles7c
 * @since 2023/2/18 21:57
 */
@Data
@NoArgsConstructor
@TableName("sys_role_dept")
public class SysRoleDept {

    @TableId
    private Long id;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 部门 ID
     */
    private Long deptId;

    public SysRoleDept(Long roleId, Long deptId) {
        this.roleId = roleId;
        this.deptId = deptId;
    }
}
