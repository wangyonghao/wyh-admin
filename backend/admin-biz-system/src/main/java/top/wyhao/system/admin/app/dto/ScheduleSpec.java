package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 白话调度配置。后端据此生成 Quartz Cron 与中文说明。
 */
@Data
@Schema(description = "调度时间")
public class ScheduleSpec {

    /** DAILY / WEEKLY / MONTHLY / INTERVAL / CRON */
    @NotBlank(message = "请选择执行频率")
    private String mode;

    private Integer hour;
    private Integer minute;
    /** 1=周一 … 7=周日 */
    private List<Integer> daysOfWeek;
    private Integer dayOfMonth;
    private Integer interval;
    /** MINUTE / HOUR */
    private String intervalUnit;
    private String cron;
}
