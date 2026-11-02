
package top.wyhao.system.admin.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告日志实体
 *

 * @since 2025/5/18 19:16
 */
@Data
@NoArgsConstructor
public class NoticeLog {

    /**
     * 公告 ID
     */
    private Long noticeId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 读取时间
     */
    private LocalDateTime readTime;

    public NoticeLog(Long noticeId, Long userId, LocalDateTime readTime) {
        this.noticeId = noticeId;
        this.userId = userId;
        this.readTime = readTime;
    }
}