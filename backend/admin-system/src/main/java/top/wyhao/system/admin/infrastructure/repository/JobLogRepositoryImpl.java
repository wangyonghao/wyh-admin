package top.wyhao.system.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.gateway.JobLogRepository;
import top.wyhao.system.admin.domain.model.JobLog;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysJobLogDO;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysJobLogMapper;

/**
 * 定时任务执行日志仓储实现
 */
@Repository
@RequiredArgsConstructor
public class JobLogRepositoryImpl implements JobLogRepository {

    private final SysJobLogMapper sysJobLogMapper;

    @Override
    public void insert(JobLog log) {
        SysJobLogDO data = AdminDoMapper.toDo(log, SysJobLogDO.class);
        sysJobLogMapper.insert(data);
        AdminDoMapper.copyBack(data, log);
    }

    @Override
    public void updateById(JobLog log) {
        SysJobLogDO data = AdminDoMapper.toDo(log, SysJobLogDO.class);
        sysJobLogMapper.updateById(data);
        AdminDoMapper.copyBack(data, log);
    }

    @Override
    public JobLog findLatestRunning(Long jobId, int runningStatus) {
        return AdminDoMapper.toDomain(sysJobLogMapper.selectOne(
                new LambdaQueryWrapper<SysJobLogDO>()
                        .eq(SysJobLogDO::getJobId, jobId)
                        .eq(SysJobLogDO::getStatus, runningStatus)
                        .orderByDesc(SysJobLogDO::getStartTime)
                        .last("LIMIT 1")), JobLog.class);
    }

    @Override
    public PageResult<JobLog> page(long page, long pageSize, Long jobId, String handlerCode, Integer status) {
        return PageResult.of(sysJobLogMapper.selectPage(
                        new Page<>(page, pageSize),
                        new LambdaQueryWrapper<SysJobLogDO>()
                                .eq(jobId != null, SysJobLogDO::getJobId, jobId)
                                .eq(StringUtils.hasText(handlerCode), SysJobLogDO::getHandlerCode, handlerCode)
                                .eq(status != null, SysJobLogDO::getStatus, status)
                                .orderByDesc(SysJobLogDO::getStartTime)))
                .map(item -> AdminDoMapper.toDomain(item, JobLog.class));
    }
}
