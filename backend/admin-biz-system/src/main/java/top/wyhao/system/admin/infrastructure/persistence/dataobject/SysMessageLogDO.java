
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息日志实体
 *


 * @since 2023/10/15 20:25
 */
@Data
@NoArgsConstructor
@TableName("sys_message_log")
public class SysMessageLogDO {

    @TableId
    private Long id;

    /**
     * 消息 ID
     */
    private Long messageId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 读取时间
     */
    private LocalDateTime readTime;

    public SysMessageLogDO(Long messageId, Long userId, LocalDateTime readTime) {
        this.messageId = messageId;
        this.userId = userId;
        this.readTime = readTime;
    }
}