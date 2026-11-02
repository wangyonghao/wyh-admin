package top.wyhao.system.admin.app.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "任务执行日志")
public class JobLogResponse {

    private String id;
    private String jobId;
    private String jobName;
    private String handlerCode;
    private String triggerType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private Integer status;
    private String errorMessage;
}
