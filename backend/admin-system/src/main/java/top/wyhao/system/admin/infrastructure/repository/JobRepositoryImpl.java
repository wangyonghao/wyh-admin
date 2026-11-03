package top.wyhao.system.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.gateway.JobRepository;
import top.wyhao.system.admin.domain.model.Job;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysJobDO;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysJobMapper;

import java.util.List;

/**
 * 定时任务仓储实现
 */
@Repository
@RequiredArgsConstructor
public class JobRepositoryImpl implements JobRepository {

    private final SysJobMapper sysJobMapper;

    @Override
    public Job findById(Long id) {
        return AdminDoMapper.toDomain(sysJobMapper.selectById(id), Job.class);
    }

    @Override
    public void insert(Job job) {
        SysJobDO data = AdminDoMapper.toDo(job, SysJobDO.class);
        sysJobMapper.insert(data);
        AdminDoMapper.copyBack(data, job);
    }

    @Override
    public void updateById(Job job) {
        SysJobDO data = AdminDoMapper.toDo(job, SysJobDO.class);
        sysJobMapper.updateById(data);
        AdminDoMapper.copyBack(data, job);
    }

    @Override
    public void deleteById(Long id) {
        sysJobMapper.deleteById(id);
    }

    @Override
    public List<Job> listAll() {
        return AdminDoMapper.toDomainList(sysJobMapper.selectList(null), Job.class);
    }

    @Override
    public PageResult<Job> page(long page, long pageSize, String name, String handlerCode, Integer status) {
        return PageResult.of(sysJobMapper.selectPage(
                        new Page<>(page, pageSize),
                        new LambdaQueryWrapper<SysJobDO>()
                                .like(StringUtils.hasText(name), SysJobDO::getName, name)
                                .eq(StringUtils.hasText(handlerCode), SysJobDO::getHandlerCode, handlerCode)
                                .eq(status != null, SysJobDO::getStatus, status)
                                .orderByDesc(SysJobDO::getCreateTime)))
                .map(item -> AdminDoMapper.toDomain(item, Job.class));
    }
}
