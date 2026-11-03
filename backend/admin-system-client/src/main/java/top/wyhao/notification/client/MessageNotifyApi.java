package top.wyhao.notification.client;

import java.util.List;

/**
 * 站内消息通知 API（系统模块实现）
 */
public interface MessageNotifyApi {

    /**
     * 发送消息给指定用户（无跳转路径）
     */
    void notifyUsers(String title, String content, String type, List<String> userIds);

    /**
     * 发送消息给指定用户，可带前端跳转路径（如个人中心改密页）
     */
    void notifyUsers(String title, String content, String type, String path, List<String> userIds);
}
