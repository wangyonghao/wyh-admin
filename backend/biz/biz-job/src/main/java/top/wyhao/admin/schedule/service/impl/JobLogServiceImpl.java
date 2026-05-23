
package top.wyhao.admin.schedule.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.admin.schedule.api.JobBatchApi;
import top.wyhao.admin.schedule.api.JobClient;
import top.wyhao.admin.schedule.model.query.JobLogQuery;
import top.wyhao.admin.schedule.model.resp.JobLogResp;
import top.wyhao.admin.schedule.service.JobLogService;
import top.wyhao.starter.web.core.model.PageResult;

/**
 * 任务日志业务实现


 * @since 2024/6/27 22:54
 */
@Service
@RequiredArgsConstructor
public class JobLogServiceImpl implements JobLogService {

    private final JobClient jobClient;
    private final JobBatchApi jobBatchApi;

    @Override
    public PageResult<JobLogResp> page(JobLogQuery query) {
        return jobClient.requestPage(() -> jobBatchApi.page(query));
    }

    @Override
    public boolean stop(Long id) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobBatchApi.stop(id)));
    }

    @Override
    public boolean retry(Long id) {
        return Boolean.TRUE.equals(jobClient.request(() -> jobBatchApi.retry(id)));
    }
}
