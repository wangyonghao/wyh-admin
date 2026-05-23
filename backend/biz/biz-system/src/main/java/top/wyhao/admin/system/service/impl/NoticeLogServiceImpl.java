
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.entity.SysNoticeLog;
import top.wyhao.admin.system.mapper.SysNoticeLogMapper;
import top.wyhao.admin.system.service.NoticeLogService;
import top.wyhao.starter.core.util.CollUtils;

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
            .select(SysNoticeLog::getUserId)
            .eq(SysNoticeLog::getNoticeId, noticeId)
            .list()
            .stream()
            .map(SysNoticeLog::getUserId)
            .toList();
        Collection<Long> subtract = CollUtil.subtract(userIds, oldUserIdList);
        if (CollUtil.isEmpty(subtract)) {
            return false;
        }
        // 新增没有关联的
        LocalDateTime now = LocalDateTime.now();
        List<SysNoticeLog> list = CollUtils.mapToList(subtract, userId -> new SysNoticeLog(noticeId, userId, now));
        return baseMapper.insertBatch(list);
    }

    @Override
    public void deleteByNoticeIds(List<Long> noticeIds) {
        if (CollUtil.isEmpty(noticeIds)) {
            return;
        }
        baseMapper.lambdaUpdate().in(SysNoticeLog::getNoticeId, noticeIds).remove();
    }

    @Override
    public List<Long> listUserIdByNoticeId(Long noticeId) {
        return baseMapper.lambdaQuery()
            .select(SysNoticeLog::getUserId)
            .eq(SysNoticeLog::getNoticeId, noticeId)
            .list()
            .stream()
            .map(SysNoticeLog::getUserId)
            .toList();
    }
}
