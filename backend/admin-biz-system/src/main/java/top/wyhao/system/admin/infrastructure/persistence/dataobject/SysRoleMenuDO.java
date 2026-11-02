
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色和菜单实体
 *

 * @since 2023/2/15 20:20
 */
@Data
@NoArgsConstructor
@TableName("sys_role_menu")
public class SysRoleMenuDO {
    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 菜单 ID
     */
    private Long menuId;

    public SysRoleMenuDO(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
