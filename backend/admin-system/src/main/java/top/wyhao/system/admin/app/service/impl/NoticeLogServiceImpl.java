
package top.wyhao.system.admin.app.service.impl;

import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysNoticeLogDO;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.admin.domain.model.NoticeLog;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysNoticeLogMapper;
import top.wyhao.system.admin.app.service.NoticeLogService;
import top.wyhao.cmn.core.util.CollUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 公告日志业务实现
 *

 * @since 2025/5/18 19:15
 */
@Service
@RequiredArgsConstructor
public class NoticeLogServiceImpl implements NoticeLogService {

    private final SysNoticeLogMapper baseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(List<Long> userIds, Long noticeId) {
        // 检查是否有变更
        List<Long> oldUserIdList = baseMapper.lambdaQuery()
            .select(SysNoticeLogDO::getUserId)
            .eq(SysNoticeLogDO::getNoticeId, noticeId)
            .list()
            .stream()
            .map(SysNoticeLogDO::getUserId)
            .toList();
        Collection<Long> subtract = CollUtil.subtract(userIds, oldUserIdList);
        if (CollUtil.isEmpty(subtract)) {
            return false;
        }
        // 新增没有关联的
        LocalDateTime now = LocalDateTime.now();
        List<NoticeLog> list = CollUtils.mapToList(subtract, userId -> new NoticeLog(noticeId, userId, now));
        return baseMapper.insertBatch(AdminDoMapper.toDoList(list, SysNoticeLogDO.class));
    }

    @Override
    public void deleteByNoticeIds(List<Long> noticeIds) {
        if (CollUtil.isEmpty(noticeIds)) {
            return;
        }
        baseMapper.lambdaUpdate().in(SysNoticeLogDO::getNoticeId, noticeIds).remove();
    }

    @Override
    public List<Long> listUserIdByNoticeId(Long noticeId) {
        return baseMapper.lambdaQuery()
            .select(SysNoticeLogDO::getUserId)
            .eq(SysNoticeLogDO::getNoticeId, noticeId)
            .list()
            .stream()
            .map(SysNoticeLogDO::getUserId)
            .toList();
    }
}
