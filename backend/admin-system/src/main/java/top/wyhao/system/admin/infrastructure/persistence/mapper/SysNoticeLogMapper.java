
package top.wyhao.system.admin.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysNoticeLogDO;
import top.wyhao.mybatis.model.BaseMapper;

/**
 * 公告日志 Mapper
 *

 * @since 2025/5/18 19:17
 */
@Mapper
public interface SysNoticeLogMapper extends BaseMapper<SysNoticeLogDO> {
}
