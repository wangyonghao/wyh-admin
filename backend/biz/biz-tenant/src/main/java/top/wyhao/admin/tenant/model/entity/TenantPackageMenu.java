
package top.wyhao.admin.tenant.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 套餐和菜单关联实体
 *
 * @author Charles7c
 * @since 2025/7/11 22:01
 */
@Data
@NoArgsConstructor
@TableName("tenant_package_menu")
public class TenantPackageMenu implements Serializable {

    @TableId
    private Long id;

    /**
     * 套餐 ID
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