package top.wyhao.admin.system.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wyhao.admin.system.model.query.LoginLogQuery;
import top.wyhao.admin.system.model.vo.log.LoginLogResult;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;

/**
 * 登录日志 Service
 *

 * @since 2026/05/08
 */
public interface LoginLogService {

    /**
     * 异步记录登录日志
     *
     * @param username      用户名
     * @param ipAddress     IP地址
     * @param userAgent     User-Agent
     * @param loginStatus   登录状态
     * @param failureReason 失败原因（可选）
     */
    void asyncLog(String username, String ipAddress, String userAgent, String loginStatus, String failureReason);

    /**
     * 分页查询登录日志
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<LoginLogResult> page(LoginLogQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    LoginLogResult detail(Long id);

    /**
     * 导出登录日志
     *
     * @param query    查询条件
     * @param response 响应对象
     */
    void export(LoginLogQuery query, HttpServletResponse response);

    /**
     * 清理过期日志
     *
     * @param retentionDays 留存天数
     * @return 清理数量
     */
    int cleanExpiredLogs(int retentionDays);
}
