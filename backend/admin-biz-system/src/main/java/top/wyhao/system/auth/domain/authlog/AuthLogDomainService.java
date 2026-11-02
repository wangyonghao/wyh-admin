package top.wyhao.system.auth.domain.authlog;

import top.wyhao.mybatis.query.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志仓储。
 */
public interface AuthLogDomainService {

    void insert(AuthLog loginLog);

    PageResult<AuthLog> page(AuthLogCriteria criteria, long page, long pageSize);

    List<AuthLog> list(AuthLogCriteria criteria);

    int deleteBefore(LocalDateTime expireTime);
}
