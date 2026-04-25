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

import cn.crane4j.annotation.AutoOperate;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.model.enums.MessageType;
import top.wyhao.admin.system.model.enums.NoticeScopes;
import top.wyhao.admin.system.mapper.MessageLogMapper;
import top.wyhao.admin.system.mapper.MessageMapper;
import top.wyhao.admin.system.model.entity.MessageDO;
import top.wyhao.admin.system.model.entity.MessageLogDO;
import top.wyhao.admin.system.model.query.MessageQuery;
import top.wyhao.admin.system.model.bo.MessageReq;
import top.wyhao.admin.system.model.vo.message.MessageDetailResp;
import top.wyhao.admin.system.model.vo.message.MessageResp;
import top.wyhao.admin.system.model.vo.message.MessageTypeUnreadResp;
import top.wyhao.admin.system.model.vo.message.MessageUnreadResp;
import top.wyhao.admin.system.service.MessageService;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.messaging.websocket.util.WebSocketUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息业务实现
 *
 * @author Bull-BCLS
 * @author Charles7c
 * @since 2023/10/15 19:05
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper baseMapper;
    private final MessageLogMapper messageLogMapper;

    @Override
    @AutoOperate(type = MessageResp.class, on = "list")
    public PageResult<MessageResp> page(MessageQuery query, PageQuery pageQuery) {
        IPage<MessageResp> page = baseMapper.selectMessagePage(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), query);
        return PageResult.build(page);
    }

    @Override
    public MessageDetailResp get(Long id) {
        return baseMapper.selectMessageById(id);
    }

    @Override
    public void readMessage(List<Long> ids, Long userId) {
        // 查询当前用户的未读消息
        List<MessageDO> list = baseMapper.selectUnreadListByUserId(userId);
        List<Long> unreadIds = CollUtils.mapToList(list, MessageDO::getId);
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
        List<MessageLogDO> list = CollUtils
                .mapToList(messageIds, messageId -> new MessageLogDO(messageId, userId, LocalDateTime.now()));
        messageLogMapper.insert(list);
    }

    @Override
    public MessageUnreadResp countUnreadByUserId(Long userId, Boolean isDetail) {
        MessageUnreadResp result = new MessageUnreadResp();
        Long total = 0L;
        if (Boolean.TRUE.equals(isDetail)) {
            List<MessageTypeUnreadResp> detailList = new ArrayList<>();
            for (MessageType messageType : MessageType.values()) {
                MessageTypeUnreadResp resp = new MessageTypeUnreadResp();
                resp.setType(messageType);
                Long count = baseMapper.selectUnreadCountByUserIdAndType(userId, messageType.getValue());
                resp.setCount(count);
                detailList.add(resp);
                total += count;
            }
            result.setDetails(detailList);
        } else {
            total = baseMapper.selectUnreadCountByUserIdAndType(userId, null);
        }
        result.setTotal(total);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MessageReq req, List<String> userIdList) {
        MessageDO message = BeanUtil.copyProperties(req, MessageDO.class);
        message.setScope(CollUtil.isEmpty(userIdList) ? NoticeScopes.ALL : NoticeScopes.USER);
        message.setUsers(userIdList);
        baseMapper.insert(message);
        // 发送消息给指定在线用户
        if (CollUtil.isNotEmpty(userIdList)) {
            userIdList.parallelStream().forEach(userId -> {
                List<String> tokenList = StpUtil.getTokenValueListByLoginId(userId);
                tokenList.parallelStream().forEach(token -> WebSocketUtils.sendMessage(token, "1"));
            });
            return;
        }
        // 发送消息给所有在线用户
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
        messageLogMapper.lambdaUpdate().in(MessageLogDO::getMessageId, messageIds).remove();
    }
}