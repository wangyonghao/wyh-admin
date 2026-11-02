package top.wyhao.system.admin.domain.gateway;

import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.model.JobLog;

/**
 * 定时任务执行日志仓储
 */
public interface JobLogRepository {

    void insert(JobLog log);

    void updateById(JobLog log);

    /**
     * 查询指定任务最近一条运行中的日志
     */
    JobLog findLatestRunning(Long jobId, int runningStatus);

    PageResult<JobLog> page(long page, long pageSize, Long jobId, String handlerCode, Integer status);
}
