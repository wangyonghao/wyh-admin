
package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.admin.system.entity.SysNoticeLog;
import top.wyhao.cmn.db.model.BaseMapper;

/**
 * 公告日志 Mapper
 *
 * @author Charles7c
 * @since 2025/5/18 19:17
 */
@Mapper
public interface NoticeLogMapper extends BaseMapper<SysNoticeLog> {
}
