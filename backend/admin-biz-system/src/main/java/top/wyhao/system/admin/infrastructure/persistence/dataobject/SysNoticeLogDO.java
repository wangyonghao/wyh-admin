
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("sys_notice_log")
public class SysNoticeLogDO {

    /**
     * 公告 ID
     */
    @TableId
    private Long noticeId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 读取时间
     */
    private LocalDateTime readTime;

    public SysNoticeLogDO(Long noticeId, Long userId, LocalDateTime readTime) {
        this.noticeId = noticeId;
        this.userId = userId;
        this.readTime = readTime;
    }
}