package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.dto.MessageRequest;
import top.wyhao.system.admin.domain.model.MessageType;
import top.wyhao.system.admin.app.service.MessageService;
import top.wyhao.notification.client.MessageNotifyApi;

import java.util.List;

/**
 * 站内消息通知 API 实现
 */
@Service
@RequiredArgsConstructor
public class MessageNotifyApiImpl implements MessageNotifyApi {

    private final MessageService messageService;

    @Override
    public void notifyUsers(String title, String content, String type, List<String> userIds) {
        notifyUsers(title, content, type, null, userIds);
    }

    @Override
    public void notifyUsers(String title, String content, String type, String path, List<String> userIds) {
        MessageType messageType;
        try {
            messageType = MessageType.valueOf(type);
        } catch (Exception e) {
            messageType = MessageType.SECURITY;
        }
        messageService.add(new MessageRequest(title, content, messageType, path), userIds);
    }
}
