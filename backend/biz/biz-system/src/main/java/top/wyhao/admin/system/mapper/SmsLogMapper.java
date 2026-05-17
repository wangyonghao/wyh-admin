
package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.cmn.db.model.BaseMapper;
import top.wyhao.admin.system.entity.SysSmsLog;

/**
 * 短信日志 Mapper
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Mapper
public interface SmsLogMapper extends BaseMapper<SysSmsLog> {}