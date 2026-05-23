
package top.wyhao.admin.schedule.service;

import top.wyhao.admin.schedule.model.query.JobQuery;
import top.wyhao.admin.schedule.model.req.JobReq;
import top.wyhao.admin.schedule.model.req.JobStatusReq;
import top.wyhao.admin.schedule.model.req.JobTriggerReq;
import top.wyhao.admin.schedule.model.resp.JobResp;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 任务业务接口


 * @since 2024/6/25 17:20
 */
public interface JobService {

    /**
     * 分页查询列表
     *
     * @param query 查询条件
     * @return 分页列表信息
     */
    PageResult<JobResp> page(JobQuery query);

    /**
     * 新增
     *
     * @param req 请求参数
     * @return 新增结果
     */
    boolean create(JobReq req);

    /**
     * 修改
     *
     * @param req 请求参数
     * @param id  ID
     * @return 修改结果
     */
    boolean update(JobReq req, Long id);

    /**
     * 修改状态
     *
     * @param req 请求参数
     * @param id  ID
     * @return 修改状态结果
     */
    boolean updateStatus(JobStatusReq req, Long id);

    /**
     * 删除
     *
     * @param id ID
     * @return 删除结果
     */
    boolean delete(Long id);

    /**
     * 执行
     *
     * @param req 请求参数
     * @return 执行结果
     */
    boolean trigger(JobTriggerReq req);

    /**
     * 查询分组列表
     *
     * @return 分组列表
     */
    List<String> listGroup();
}
