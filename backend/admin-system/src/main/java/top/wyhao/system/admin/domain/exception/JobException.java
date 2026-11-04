package top.wyhao.system.admin.domain.exception;

import top.wyhao.cmn.core.exception.BizException;

/**
 * 定时任务业务异常
 */
public class JobException extends BizException {

    public JobException(String message) {
        super(message);
    }

    public JobException(String code, String message) {
        super(code, message);
    }

    public static JobException of(String message) {
        return new JobException(message);
    }

    public static JobException of(String code, String message) {
        return new JobException(code, message);
    }

    public static JobException scheduleModeRequired() {
        return of("JOB_SCHEDULE_MODE_REQUIRED", "请选择执行频率");
    }

    public static JobException weekdayRequired() {
        return of("JOB_WEEKDAY_REQUIRED", "请选择星期");
    }

    public static JobException weekdayInvalid() {
        return of("JOB_WEEKDAY_INVALID", "星期取值应为 1（周一）到 7（周日）");
    }

    public static JobException dayOfMonthRequired() {
        return of("JOB_DAY_OF_MONTH_REQUIRED", "请选择每月日期（1-31）");
    }

    public static JobException intervalInvalid() {
        return of("JOB_INTERVAL_INVALID", "间隔必须大于 0");
    }

    public static JobException cronRequired() {
        return of("JOB_CRON_REQUIRED", "请填写 Cron 表达式");
    }

    public static JobException cronInvalid() {
        return of("JOB_CRON_INVALID", "Cron 表达式无效");
    }

    public static JobException scheduleModeUnsupported() {
        return of("JOB_SCHEDULE_MODE_UNSUPPORTED", "不支持的执行频率");
    }

    public static JobException generatedCronInvalid() {
        return of("JOB_GENERATED_CRON_INVALID", "生成的 Cron 无效");
    }

    public static JobException hourRequired() {
        return of("JOB_HOUR_REQUIRED", "请选择小时（0-23）");
    }

    public static JobException minuteRequired() {
        return of("JOB_MINUTE_REQUIRED", "请选择分钟（0-59）");
    }

    public static JobException scheduleSerializeFailed() {
        return of("JOB_SCHEDULE_SERIALIZE_FAILED", "调度配置序列化失败");
    }

    public static JobException statusInvalid() {
        return of("JOB_STATUS_INVALID", "状态无效");
    }

    public static JobException notFound() {
        return of("JOB_NOT_FOUND", "任务不存在");
    }
}
