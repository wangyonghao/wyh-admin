package top.wyhao.system.auth.app.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.auth.app.dto.AuthenticationLogQuery;
import top.wyhao.system.auth.app.dto.AuthenticationLogResult;
import top.wyhao.web.core.model.PageQuery;

/**
 * 登录日志 Service（查询 / 导出 / 清理；写路径见 AuthLogGateway）
 *
 * @since 2026/05/08
 */
public interface AuthLogService {

    /**
     * 分页查询登录日志
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<AuthenticationLogResult> page(AuthenticationLogQuery query, PageQuery pageQuery);

    /**
     * 导出登录日志
     *
     * @param query    查询条件
     * @param response 响应对象
     */
    void export(AuthenticationLogQuery query, HttpServletResponse response);

    /**
     * 清理过期日志
     *
     * @param retentionDays 留存天数
     * @return 清理数量
     */
    int cleanExpiredLogs(int retentionDays);
}
