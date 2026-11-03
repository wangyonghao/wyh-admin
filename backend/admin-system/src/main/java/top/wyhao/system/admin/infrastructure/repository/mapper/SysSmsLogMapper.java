
package top.wyhao.system.admin.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysSmsLogDO;
import top.wyhao.mybatis.model.BaseMapper;

/**
 * 短信日志 Mapper
 *

 * @since 2025/03/15 22:15
 */
@Mapper
public interface SysSmsLogMapper extends BaseMapper<SysSmsLogDO> {}