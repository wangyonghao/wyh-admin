
package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.admin.system.entity.SysMessageLog;
import top.wyhao.cmn.db.model.BaseMapper;

/**
 * 消息日志 Mapper
 *
 * @author Bull-BCLS
 * @author Charles7c
 * @since 2023/10/15 20:25
 */
@Mapper
public interface MessageLogMapper extends BaseMapper<SysMessageLog> {
}