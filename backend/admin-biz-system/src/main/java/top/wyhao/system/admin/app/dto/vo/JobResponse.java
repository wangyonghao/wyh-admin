package top.wyhao.system.admin.app.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import top.wyhao.system.admin.app.dto.ScheduleSpec;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "定时任务")
public class JobResponse {

    private String id;
    private String name;
    private String handlerCode;
    private String handlerName;
    private String handlerDescription;
    private String cron;
    private String scheduleMode;
    private ScheduleSpec schedule;
    private String scheduleLabel;
    private String params;
    private Integer status;
    private String remark;
    private LocalDateTime nextFireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> upcomingTimes;
}
