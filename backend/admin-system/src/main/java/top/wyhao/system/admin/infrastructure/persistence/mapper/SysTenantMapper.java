
package top.wyhao.system.admin.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantDO;
import top.wyhao.mybatis.model.BaseMapper;

/**
 * 租户 Mapper
 *

 * @since 2024/11/26 17:20
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<TenantDO> {

}