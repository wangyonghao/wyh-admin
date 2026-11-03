
package top.wyhao.system.admin.app.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.web.log.OperationLog;
import top.wyhao.system.admin.app.dto.OperationLogDetailResult;
import top.wyhao.system.admin.app.dto.OperationLogQuery;
import top.wyhao.system.admin.app.dto.OperationLogResult;

/**
 * 操作日志 Service
 */
public interface OperationLogService {

    @Async
    @EventListener
    void create(OperationLog operationLog);

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<OperationLogResult> page(OperationLogQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    OperationLogDetailResult detail(Long id);

    /**
     * 导出操作日志
     *
     * @param query     查询条件
     * @param response  响应对象
     */
    void export(OperationLogQuery query, HttpServletResponse response);
}
