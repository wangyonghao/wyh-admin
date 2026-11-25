package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 套餐待发布菜单。
 */
@Data
@NoArgsConstructor
@TableName("tenant_package_menu_draft")
public class TenantPackageMenuDraftDO {

    @TableId(value = "package_id", type = IdType.INPUT)
    private Long packageId;

    private Long menuId;

    public TenantPackageMenuDraftDO(Long packageId, Long menuId) {
        this.packageId = packageId;
        this.menuId = menuId;
    }
}
