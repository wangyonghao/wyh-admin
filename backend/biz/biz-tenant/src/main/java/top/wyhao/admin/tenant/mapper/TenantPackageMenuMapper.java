
package top.wyhao.admin.tenant.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.admin.tenant.model.entity.TenantPackageMenu;
import top.wyhao.cmn.db.model.BaseMapper;

/**
 * 套餐和菜单关联 Mapper
 *

 * @since 2025/7/13 20:24
 */
@Mapper
public interface TenantPackageMenuMapper extends BaseMapper<TenantPackageMenu> {
}
