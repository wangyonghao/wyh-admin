
package top.wyhao.system.admin.app.service.impl;

import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMessageLogDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMessageDO;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.admin.domain.model.Message;
import top.wyhao.system.admin.domain.model.MessageLog;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysMessageLogMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysMessageMapper;
import top.wyhao.system.admin.domain.model.MessageType;
import top.wyhao.system.admin.domain.model.NoticeScopes;
import top.wyhao.system.admin.app.service.MessageService;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.starter.messaging.websocket.util.WebSocketUtils;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import top.wyhao.system.admin.app.dto.MessageDetailResult;
import top.wyhao.system.admin.app.dto.MessageQuery;
import top.wyhao.system.admin.app.dto.MessageRequest;
import top.wyhao.system.admin.app.dto.MessageResult;
import top.wyhao.system.admin.app.dto.MessageUnreadCountResult;
import top.wyhao.system.admin.app.dto.MessageUnreadResult;

/**
 * 消息业务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final SysMessageMapper baseMapper;
    private final SysMessageLogMapper messageLogMapper;

    @Override
    public PageResult<MessageDetailResult> page(MessageQuery query, PageQuery pageQuery) {
        IPage<MessageDetailResult> page = baseMapper.selectMessagePage(new Page<>(pageQuery.getPage(), pageQuery
            .getPageSize()), query);
        return PageResult.of(page);
    }

    @Override
    public MessageResult get(Long id) {
        return baseMapper.selectMessageById(id);
    }

    @Override
    public void readMessage(List<Long> ids, Long userId) {
        List<Message> list = AdminDoMapper.toDomainList(baseMapper.selectUnreadListByUserId(userId), Message.class);
        List<Long> unreadIds = CollUtils.mapToList(list, Message::getId);
        this.addWithUserId(CollUtil.isNotEmpty(ids)
            ? CollUtil.intersection(unreadIds, ids).stream().toList()
            : unreadIds, userId);
        WebSocketUtils.sendMessage(StpUtil.getTokenValueByLoginId(userId), String.valueOf(baseMapper
            .selectUnreadListByUserId(userId)
            .size()));
    }

    private void addWithUserId(List<Long> messageIds, Long userId) {
        if (CollUtil.isEmpty(messageIds)) {
            return;
        }
        List<MessageLog> list = CollUtils
                .mapToList(messageIds, messageId -> new MessageLog(messageId, userId, LocalDateTime.now()));
        messageLogMapper.insert(AdminDoMapper.toDoList(list, SysMessageLogDO.class));
    }

    @Override
    public MessageUnreadResult countUnreadByUserId(Long userId, Boolean isDetail) {
        Long total = 0L;
        List<MessageUnreadCountResult> detailList = null;
        if (Boolean.TRUE.equals(isDetail)) {
            detailList = new ArrayList<>();
            for (MessageType messageType : MessageType.values()) {
                Long count = baseMapper.selectUnreadCountByUserIdAndType(userId, messageType.getValue());
                detailList.add(new MessageUnreadCountResult(messageType, count));
                total += count;
            }
        } else {
            total = baseMapper.selectUnreadCountByUserIdAndType(userId, null);
        }
        return new MessageUnreadResult(total, detailList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MessageRequest req, List<String> userIdList) {
        Message message = new Message();
        message.setTitle(req.getTitle());
        message.setContent(req.getContent());
        message.setType(req.getType());
        message.setPath(req.getPath());
        message.setScope(CollUtil.isEmpty(userIdList) ? NoticeScopes.ALL : NoticeScopes.USER);
        message.setUsers(userIdList);
        SysMessageDO data = AdminDoMapper.toDo(message, SysMessageDO.class);
        baseMapper.insert(data);
        AdminDoMapper.copyBack(data, message);
        if (CollUtil.isNotEmpty(userIdList)) {
            userIdList.parallelStream().forEach(userId -> {
                List<String> tokenList = StpUtil.getTokenValueListByLoginId(userId);
                tokenList.parallelStream().forEach(token -> WebSocketUtils.sendMessage(token, "1"));
            });
            return;
        }
        WebSocketUtils.sendMessage("1");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        baseMapper.deleteByIds(ids);
        this.deleteByMessageIds(ids);
    }

    private void deleteByMessageIds(List<Long> messageIds) {
        if (CollUtil.isEmpty(messageIds)) {
            return;
        }
        messageLogMapper.lambdaUpdate().in(SysMessageLogDO::getMessageId, messageIds).remove();
    }
}
