
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.entity.SysNotice;
import top.wyhao.admin.system.mapper.SysNoticeMapper;
import top.wyhao.admin.system.model.bo.MessageRequest;
import top.wyhao.admin.system.model.bo.NoticeRequest;
import top.wyhao.admin.system.model.enums.*;
import top.wyhao.admin.system.model.query.NoticeQuery;
import top.wyhao.admin.system.model.vo.NoticeDetailResult;
import top.wyhao.admin.system.model.vo.NoticeResult;
import top.wyhao.admin.system.model.vo.dashboard.DashboardNoticeResp;
import top.wyhao.admin.system.service.MessageService;
import top.wyhao.admin.system.service.NoticeLogService;
import top.wyhao.admin.system.service.NoticeService;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.exception.BadRequestException;
import top.wyhao.starter.core.exception.SystemException;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告管理 Service 实现
 *

 * @since 2026/5/8
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final SysNoticeMapper noticeMapper;
    private final NoticeLogService noticeLogService;
    private final MessageService messageService;


    @Override
    public PageResult<NoticeResult> page(NoticeQuery query, PageQuery pageQuery) {
        IPage<NoticeResult> page = noticeMapper.selectNoticePage(new Page<>(pageQuery.getPage(), pageQuery
                .getSize()), query);
        return PageResult.build(page);
    }

    @Override
    public NoticeDetailResult detail(Long id) {
        SysNotice entity = noticeMapper.selectById(id);
        if (entity == null) {
            throw new BadRequestException("NOTICE_NOT_FOUND", "公告不存在");
        }
        // 将 SysNotice 转换为 NoticeDetailResp
        return convertToNoticeDetailResp(entity);
    }

    @Override
    public Long create(NoticeRequest req) {
        if (!NoticeStatus.DRAFT.equals(req.getStatus())) {
            if (Boolean.TRUE.equals(req.getIsTiming())) {
                // 待发布
                req.setStatus(NoticeStatus.PENDING);
            } else {
                // 已发布
                req.setStatus(NoticeStatus.PUBLISHED);
                req.setPublishTime(LocalDateTime.now());
            }
        }
        SysNotice entity = new SysNotice();
        // 设置实体属性
        updateEntityFromReq(entity, req);
        int result = noticeMapper.insert(entity);
        if (result <= 0) {
            throw new SystemException("创建失败");
        }
        // 发送消息
        if (NoticeStatus.PUBLISHED.equals(entity.getStatus())) {
            this.publish(entity);
        }
        return entity.getId();
    }

    @Override
    public void update(NoticeRequest req, Long id) {
        SysNotice oldNotice = noticeMapper.selectById(id);
        switch (oldNotice.getStatus()) {
            case PUBLISHED -> {
                BizAssert.throwIfNotEqual(req.getStatus(), oldNotice.getStatus(), "公告已发布，不允许修改状态");
                BizAssert.throwIfNotEqual(req.getIsTiming(), oldNotice.getIsTiming(), "公告已发布，不允许修改定时发布信息");
                BizAssert.throwIfNotEqual(req.getNoticeScope(), oldNotice.getNoticeScope(), "公告已发布，不允许修改通知范围");
                if (NoticeScopes.USER.equals(oldNotice.getNoticeScope())) {
                    BizAssert.throwIfNotEmpty(CollUtil.disjunction(req.getNoticeUsers(), oldNotice
                            .getNoticeUsers()), "公告已发布，不允许修改通知用户");
                }
                BizAssert.isTrue(!CollUtil.isEqualList(req.getNoticeMethods(), oldNotice
                        .getNoticeMethods()), "公告已发布，不允许修改通知方式");
                // 修正定时发布信息
                if (Boolean.TRUE.equals(oldNotice.getIsTiming())) {
                    BizAssert.throwIfNotEqual(req.getPublishTime(), oldNotice.getPublishTime(), "公告已发布，不允许修改定时发布信息");
                }
                req.setPublishTime(oldNotice.getPublishTime());
            }
            case DRAFT, PENDING -> {
                // 已发布
                if (NoticeStatus.PUBLISHED.equals(req.getStatus())) {
                    if (Boolean.TRUE.equals(req.getIsTiming())) {
                        // 待发布
                        req.setStatus(NoticeStatus.PENDING);
                    } else {
                        // 已发布
                        req.setStatus(NoticeStatus.PUBLISHED);
                        req.setPublishTime(LocalDateTime.now());
                    }
                }
            }
            default -> throw new IllegalArgumentException("状态无效");
        }
        SysNotice entity = noticeMapper.selectById(id);
        if (entity == null) {
            throw new BadRequestException("NOTICE_NOT_FOUND", "公告不存在");
        }
        // 更新实体属性
        updateEntityFromReq(entity, req);
        int result = noticeMapper.updateById(entity);
        if (result <= 0) {
            throw new SystemException("更新失败");
        }
        // 重置定时发布时间
        if (!NoticeStatus.PUBLISHED.equals(entity.getStatus()) && Boolean.FALSE.equals(entity
                .getIsTiming()) && entity.getPublishTime() != null) {
            noticeMapper.lambdaUpdate().set(SysNotice::getPublishTime, null).eq(SysNotice::getId, entity.getId()).update();
        }
        // 发送消息
        if (Boolean.FALSE.equals(entity.getIsTiming()) && NoticeStatus.PUBLISHED.equals(entity.getStatus())) {
            this.publish(entity);
        }
    }

    @Override
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("REQUIRE_NONE_NULL", "ID 不能为空");
        }
        // 调用批量删除
        int result = noticeMapper.deleteByIds(ids);
        if (result <= 0) {
            throw new BadRequestException("DELETE_FAILED", "删除失败");
        }
        // 删除公告日志
        noticeLogService.deleteByNoticeIds(ids);
    }

    @Override
    public void publish(SysNotice notice) {
        List<Integer> noticeMethods = notice.getNoticeMethods();
        if (CollUtil.isNotEmpty(noticeMethods) && noticeMethods.contains(NoticeMethods.SYSTEM_MESSAGE.getValue())) {
            MessageTemplates template = MessageTemplates.NOTICE_PUBLISH;
            MessageRequest req = new MessageRequest(MessageType.SYSTEM);
            req.setTitle(template.getTitle());
            req.setContent(template.getContent().formatted(notice.getTitle()));
            req.setPath(template.getPath().formatted(notice.getId()));
            // 新增消息
            messageService.add(req, notice.getNoticeUsers());
        }
    }

    @Override
    public List<Long> listUnreadIdsByUserId(NoticeMethods method, Long userId) {
        return noticeMapper.selectUnreadIdsByUserId(method != null ? method.getValue() : null, userId);
    }

    @Override
    public void readNotice(Long id, Long userId) {
        noticeLogService.add(List.of(userId), id);
    }

    @Override
    public List<DashboardNoticeResp> listDashboard() {
        Long userId = LoginUtil.getUserId();
        return noticeMapper.selectDashboardList(userId);
    }

    private NoticeDetailResult convertToNoticeDetailResp(SysNotice entity) {
        NoticeDetailResult resp = new NoticeDetailResult();
        resp.setId(entity.getId());
        resp.setTitle(entity.getTitle());
        resp.setContent(entity.getContent());
        resp.setStatus(entity.getStatus());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateTime(entity.getUpdateTime());
        // 设置其他属性...
        return resp;
    }

    private void updateEntityFromReq(SysNotice entity, NoticeRequest req) {
        entity.setTitle(req.getTitle());
        entity.setContent(req.getContent());
        entity.setStatus(req.getStatus());
        entity.setIsTiming(req.getIsTiming());
        entity.setPublishTime(req.getPublishTime());
        entity.setNoticeScope(req.getNoticeScope());
        entity.setNoticeUsers(req.getNoticeUsers());
        entity.setNoticeMethods(req.getNoticeMethods());
        // 设置其他属性...
    }
}