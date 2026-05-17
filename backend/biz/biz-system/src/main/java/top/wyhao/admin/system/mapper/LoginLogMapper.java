package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.admin.system.entity.SysLoginLog;
import top.wyhao.cmn.db.model.BaseMapper;

/**
 * 登录日志 Mapper
 *
 * @author Yonghao Wang
 * @since 2026/05/08
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<SysLoginLog> {
}
