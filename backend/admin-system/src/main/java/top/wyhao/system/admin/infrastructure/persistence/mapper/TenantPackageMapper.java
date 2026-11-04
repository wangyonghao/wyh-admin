
package top.wyhao.system.admin.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageDO;
import top.wyhao.mybatis.model.BaseMapper;

/**
 * 套餐 Mapper
 *

 * @since 2024/11/26 11:25
 */
@Mapper
public interface TenantPackageMapper extends BaseMapper<TenantPackageDO> {
}