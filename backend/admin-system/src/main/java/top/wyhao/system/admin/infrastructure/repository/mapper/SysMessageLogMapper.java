
package top.wyhao.system.admin.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMessageLogDO;
import top.wyhao.mybatis.model.BaseMapper;

/**
 * 消息日志 Mapper
 *


 * @since 2023/10/15 20:25
 */
@Mapper
public interface SysMessageLogMapper extends BaseMapper<SysMessageLogDO> {
}