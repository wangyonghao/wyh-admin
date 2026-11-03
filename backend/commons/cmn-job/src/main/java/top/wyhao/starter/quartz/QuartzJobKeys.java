package top.wyhao.starter.quartz;

import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * Quartz Job / Trigger 命名约定。
 */
public final class QuartzJobKeys {

    public static final String GROUP = "wyh";
    public static final String JOB_ID = "jobId";
    public static final String HANDLER_CODE = "handlerCode";
    public static final String PARAMS = "params";
    public static final String MANUAL = "manual";
    public static final String START_EPOCH_MS = "_startEpochMs";

    private QuartzJobKeys() {
    }

    public static JobKey jobKey(Long jobId) {
        return JobKey.jobKey(String.valueOf(jobId), GROUP);
    }

    public static TriggerKey triggerKey(Long jobId) {
        return TriggerKey.triggerKey(String.valueOf(jobId), GROUP);
    }
}
