
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色和菜单实体
 *
 * @author Charles7c
 * @since 2023/2/15 20:20
 */
@Data
@NoArgsConstructor
@TableName("sys_role_menu")
public class SysRoleMenu {
    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 菜单 ID
     */
    private Long menuId;

    public SysRoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
