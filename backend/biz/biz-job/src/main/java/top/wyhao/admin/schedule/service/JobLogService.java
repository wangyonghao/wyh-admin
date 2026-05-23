
package top.wyhao.admin.schedule.service;

import top.wyhao.admin.schedule.model.query.JobLogQuery;
import top.wyhao.admin.schedule.model.resp.JobLogResp;
import top.wyhao.starter.web.core.model.PageResult;

/**
 * 任务日志业务接口


 * @since 2024/6/27 22:52
 */
public interface JobLogService {

    /**
     * 分页查询列表
     *
     * @param query 查询条件
     * @return 分页列表信息
     */
    PageResult<JobLogResp> page(JobLogQuery query);

    /**
     * 停止
     *
     * @param id ID
     * @return 停止结果
     */
    boolean stop(Long id);

    /**
     * 重试
     *
     * @param id ID
     * @return 重试结果
     */
    boolean retry(Long id);
}
