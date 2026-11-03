package top.wyhao.system.admin.domain.gateway;

import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.model.Job;

import java.util.List;

/**
 * 定时任务仓储
 */
public interface JobRepository {

    Job findById(Long id);

    void insert(Job job);

    void updateById(Job job);

    void deleteById(Long id);

    List<Job> listAll();

    PageResult<Job> page(long page, long pageSize, String name, String handlerCode, Integer status);
}
