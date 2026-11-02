
package top.wyhao.system.admin.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 套餐和菜单关联实体
 *

 * @since 2025/7/11 22:01
 */
@Data
@NoArgsConstructor
public class TenantPackageMenu {

    /**
     * 套餐 ID。表主键是 (package_id, menu_id)，这里只把 package_id 标成主键，避免插入不存在的 id 列。
     */
    private Long packageId;

    /**
     * 菜单 ID
     */
    private Long menuId;

    public TenantPackageMenu(Long packageId, Long menuId) {
        this.packageId = packageId;
        this.menuId = menuId;
    }
}