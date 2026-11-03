
package top.wyhao.system.admin.app.service;

import top.wyhao.system.admin.domain.model.NoticeMethods;
import top.wyhao.system.admin.domain.model.Notice;
import top.wyhao.system.admin.app.dto.DashboardNoticeResp;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import top.wyhao.system.admin.app.dto.NotificationDetailResult;
import top.wyhao.system.admin.app.dto.NotificationQuery;
import top.wyhao.system.admin.app.dto.NotificationRequest;
import top.wyhao.system.admin.app.dto.NotificationResult;

/**
 * 公告管理 API
 *

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
    PageResult<NotificationResult> page(NotificationQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    NotificationDetailResult detail(Long id);

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return 自增 ID
     */
    Long create(@Valid NotificationRequest req);

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(@Valid NotificationRequest req, Long id);

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
    void publish(Notice notice);

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