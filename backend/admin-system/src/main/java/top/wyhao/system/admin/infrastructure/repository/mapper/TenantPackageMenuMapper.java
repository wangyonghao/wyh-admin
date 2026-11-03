
package top.wyhao.system.admin.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageMenuDO;
import top.wyhao.mybatis.model.BaseMapper;

/**
 * 套餐和菜单关联 Mapper
 *

 * @since 2025/7/13 20:24
 */
@Mapper
public interface TenantPackageMenuMapper extends BaseMapper<TenantPackageMenuDO> {
}
