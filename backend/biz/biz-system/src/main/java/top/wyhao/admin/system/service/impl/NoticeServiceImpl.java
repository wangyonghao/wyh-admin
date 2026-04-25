/*
 * Copyright (c) 2022-present wangyonghao Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.wyhao.admin.system.model.enums.*;
import top.wyhao.admin.system.mapper.NoticeMapper;
import top.wyhao.admin.system.model.entity.NoticeDO;
import top.wyhao.admin.system.model.query.NoticeQuery;
import top.wyhao.admin.system.model.bo.MessageReq;
import top.wyhao.admin.system.model.bo.NoticeReq;
import top.wyhao.admin.system.model.vo.dashboard.DashboardNoticeResp;
import top.wyhao.admin.system.model.vo.notice.NoticeDetailResp;
import top.wyhao.admin.system.model.vo.notice.NoticeResp;
import top.wyhao.admin.system.service.MessageService;
import top.wyhao.admin.system.service.NoticeLogService;
import top.wyhao.admin.system.service.NoticeService;
import top.wyhao.common.security.util.LoginUtil;
import top.wyhao.starter.core.exception.BadRequestException;
import top.wyhao.starter.core.exception.SystemException;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.data.util.QueryWrapperUtil;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;
import top.wyhao.starter.web.core.model.PageResult;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 公告业务实现
 * 不再继承 BaseServiceImpl，而是直接实现 NoticeService 接口的所有方法
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final NoticeLogService noticeLogService;
    private final MessageService messageService;


    @Override
    public PageResult<NoticeResp> findPage(NoticeQuery query, PageQuery pageQuery) {
        IPage<NoticeResp> page = noticeMapper.selectNoticePage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), query);
        PageResult<NoticeResp> pageResult = PageResult.build(page);
        pageResult.getList().forEach(this::fill);
        return pageResult;
    }

    @Override
    public List<NoticeResp> list(NoticeQuery query, SortQuery sortQuery) {
        // 实现列表查询逻辑
        QueryWrapper<NoticeDO> wrapper = QueryWrapperUtil.build(query);
        // 应用排序
        QueryWrapperUtil.applySort(wrapper, sortQuery.getSort(), NoticeDO.class);

        List<NoticeDO> entities = noticeMapper.selectList(wrapper);
        // 将 NoticeDO 转换为 NoticeResp
        return entities.stream()
                .map(this::convertToNoticeResp)
                .toList();
    }

    @Override
    public NoticeDetailResp get(Long id) {
        NoticeDO entity = noticeMapper.selectById(id);
        if (entity == null) {
            throw new BadRequestException("NOTICE_NOT_FOUND","公告不存在");
        }
        // 将 NoticeDO 转换为 NoticeDetailResp
        return convertToNoticeDetailResp(entity);
    }

    @Override
    public Long create(NoticeReq req) {
        beforeCreate(req);
        NoticeDO entity = new NoticeDO();
        // 设置实体属性
        updateEntityFromReq(entity, req);
        int result = noticeMapper.insert(entity);
        if (result <= 0) {
            throw new SystemException("创建失败");
        }
        afterCreate(req, entity);
        return entity.getId();
    }

    @Override
    public void update(NoticeReq req, Long id) {
        beforeUpdate(req, id);
        NoticeDO entity = noticeMapper.selectById(id);
        if (entity == null) {
            throw new BadRequestException("NOTICE_NOT_FOUND","公告不存在");
        }
        // 更新实体属性
        updateEntityFromReq(entity, req);
        int result = noticeMapper.updateById(entity);
        if (result <= 0) {
            throw new SystemException("更新失败");
        }
        afterUpdate(req, entity);
    }

    @Override
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("REQUIRE_NONE_NULL","ID 不能为空");
        }
        // 调用批量删除
        int result = noticeMapper.deleteByIds(ids);
        if (result <= 0) {
            throw new BadRequestException("DELETE_FAILED","删除失败");
        }
        afterDelete(ids);
    }

    @Override
    public void export(NoticeQuery query, SortQuery sortQuery, HttpServletResponse response) {
        // 实现导出逻辑
        List<NoticeResp> list = list(query, sortQuery);
        // 使用Excel工具导出数据到response
    }

    @Override
    public List<LabelValueResp> dict(NoticeQuery query, SortQuery sortQuery) {
        // 实现字典查询逻辑
        List<NoticeResp> list = list(query, sortQuery);
        // 将列表转换为 LabelValueResp 格式
        return list.stream()
                .map(item -> new LabelValueResp(item.getTitle(), item.getId()))
                .toList();
    }

    private void beforeCreate(NoticeReq req) {
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
    }

    private void afterCreate(NoticeReq req, NoticeDO entity) {
        // 发送消息
        if (NoticeStatus.PUBLISHED.equals(entity.getStatus())) {
            this.publish(entity);
        }
    }

    private void beforeUpdate(NoticeReq req, Long id) {
        NoticeDO oldNotice = noticeMapper.selectById(id);
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
    }

    private void afterUpdate(NoticeReq req, NoticeDO entity) {
        // 重置定时发布时间
        if (!NoticeStatus.PUBLISHED.equals(entity.getStatus()) && Boolean.FALSE.equals(entity
            .getIsTiming()) && entity.getPublishTime() != null) {
            noticeMapper.lambdaUpdate().set(NoticeDO::getPublishTime, null).eq(NoticeDO::getId, entity.getId()).update();
        }
        // 发送消息
        if (Boolean.FALSE.equals(entity.getIsTiming()) && NoticeStatus.PUBLISHED.equals(entity.getStatus())) {
            this.publish(entity);
        }
    }

    private void afterDelete(List<Long> ids) {
        // 删除公告日志
        noticeLogService.deleteByNoticeIds(ids);
    }

    @Override
    public void publish(NoticeDO notice) {
        List<Integer> noticeMethods = notice.getNoticeMethods();
        if (CollUtil.isNotEmpty(noticeMethods) && noticeMethods.contains(NoticeMethods.SYSTEM_MESSAGE.getValue())) {
            MessageTemplates template = MessageTemplates.NOTICE_PUBLISH;
            MessageReq req = new MessageReq(MessageType.SYSTEM);
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

    // 辅助方法
    private LambdaQueryWrapper<NoticeDO> buildQueryWrapper(NoticeQuery query) {
        LambdaQueryWrapper<NoticeDO> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.hasText(query.getTitle())) {
                wrapper.like(NoticeDO::getTitle, query.getTitle());
            }
            if (query.getType() != null) {
                wrapper.eq(NoticeDO::getType, query.getType());
            }
            if (query.getUserId() != null) {
                wrapper.eq(NoticeDO::getNoticeUsers, query.getUserId());
            }
        }
        return wrapper;
    }

    private NoticeResp convertToNoticeResp(NoticeDO entity) {
        NoticeResp resp = new NoticeResp();
        resp.setId(entity.getId());
        resp.setTitle(entity.getTitle());
        resp.setStatus(entity.getStatus());
        resp.setCreateTime(entity.getCreateTime());
        return resp;
    }

    private NoticeDetailResp convertToNoticeDetailResp(NoticeDO entity) {
        NoticeDetailResp resp = new NoticeDetailResp();
        resp.setId(entity.getId());
        resp.setTitle(entity.getTitle());
        resp.setContent(entity.getContent());
        resp.setStatus(entity.getStatus());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateTime(entity.getUpdateTime());
        // 设置其他属性...
        return resp;
    }

    private void updateEntityFromReq(NoticeDO entity, NoticeReq req) {
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

    private void fill(NoticeResp noticeResp) {
        // 填充额外信息的逻辑
    }
}