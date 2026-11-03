package top.wyhao.system.admin.app.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import top.wyhao.audit.client.LoginEvent;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.LoginLogQuery;
import top.wyhao.system.admin.app.dto.LoginLogResult;
import top.wyhao.web.core.model.PageQuery;

/**
 * 登录日志 Service（事件落库 / 查询 / 导出 / 清理）
 */
public interface LoginLogService {

    /**
     * 监听 Auth 发布的登录认证事件并落库。
     */
    @Async
    @EventListener
    void onLogin(LoginEvent event);

    PageResult<LoginLogResult> page(LoginLogQuery query, PageQuery pageQuery);

    void export(LoginLogQuery query, HttpServletResponse response);

    int cleanExpiredLogs(int retentionDays);
}
