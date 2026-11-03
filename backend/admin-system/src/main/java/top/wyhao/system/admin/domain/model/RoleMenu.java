
package top.wyhao.system.admin.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色和菜单实体
 *

 * @since 2023/2/15 20:20
 */
@Data
@NoArgsConstructor
public class RoleMenu {
    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 菜单 ID
     */
    private Long menuId;

    public RoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
