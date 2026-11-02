
package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息实体
 *

 * @since 2023/10/15 19:05
 */
@Data
public class Message {

    /**
     * ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 类型
     */
    private MessageType type;

    /**
     * 跳转路径
     */
    private String path;

    /**
     * 通知范围
     */
    private NoticeScopes scope;

    /**
     * 通知用户
     */
    private List<String> users;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}