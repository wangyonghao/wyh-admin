package top.wyhao.system.admin.domain.gateway;

import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.model.LoginLog;
import top.wyhao.system.admin.domain.model.LoginLogCriteria;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志仓储。
 */
public interface LoginLogRepository {

    void insert(LoginLog loginLog);

    PageResult<LoginLog> page(LoginLogCriteria criteria, long page, long pageSize);

    List<LoginLog> list(LoginLogCriteria criteria);

    int deleteBefore(LocalDateTime expireTime);
}
