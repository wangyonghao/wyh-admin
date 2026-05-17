
package top.wyhao.admin.system.service;

import top.wyhao.admin.system.model.enums.NoticeMethods;
import top.wyhao.admin.system.entity.SysNotice;
import top.wyhao.admin.system.model.query.NoticeQuery;
import top.wyhao.admin.system.model.bo.NoticeRequest;
import top.wyhao.admin.system.model.vo.dashboard.DashboardNoticeResp;
import top.wyhao.admin.system.model.vo.NoticeDetailResult;
import top.wyhao.admin.system.model.vo.NoticeResult;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 公告管理 API
 *
 * @author Yonghao Wang
 * @since 2026/5/8
 */
public interface NoticeService {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<NoticeResult> page(NoticeQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    NoticeDetailResult detail(Long id);

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return 自增 ID
     */
    Long create(@Valid NoticeRequest req);

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(@Valid NoticeRequest req, Long id);

    /**
     * 删除
     *
     * @param ids ID 列表
     */
    void delete(@NotEmpty(message = "ID 不能为空") List<Long> ids);
    /**
     * 发布公告
     *
     * @param notice 公告信息
     */
    void publish(SysNotice notice);

    /**
     * 查询未读公告 ID 列表
     *
     * @param method 通知方式
     * @param userId 用户 ID
     * @return 未读公告 ID 响应参数
     */
    List<Long> listUnreadIdsByUserId(NoticeMethods method, Long userId);

    /**
     * 阅读公告
     *
     * @param id     公告 ID
     * @param userId 用户 ID
     */
    void readNotice(Long id, Long userId);

    /**
     * 查询仪表盘公告列表
     *
     * @return 仪表盘公告列表
     */
    List<DashboardNoticeResp> listDashboard();
}