package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务执行日志
 */
@Data
public class JobLog {

    private Long id;
    private Long jobId;
    private String jobName;
    private String handlerCode;
    /** CRON / MANUAL */
    private String triggerType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    /** 1 运行中 2 成功 3 失败 */
    private Integer status;
    private String errorMessage;
}
