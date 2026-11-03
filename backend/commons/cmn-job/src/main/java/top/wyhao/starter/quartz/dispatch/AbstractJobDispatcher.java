package top.wyhao.starter.quartz.dispatch;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import top.wyhao.starter.quartz.JobHandlerRegistry;
import top.wyhao.starter.quartz.QuartzJobKeys;
import top.wyhao.starter.quartz.spi.JobContext;

/**
 * 将 Quartz 触发分发到已注册 JobTask。
 */
public abstract class AbstractJobDispatcher extends QuartzJobBean {

    @Autowired
    private JobHandlerRegistry jobHandlerRegistry;

    @Override
    protected void executeInternal(JobExecutionContext context) throws org.quartz.JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        Long jobId = toLong(data.get(QuartzJobKeys.JOB_ID));
        String handlerCode = data.getString(QuartzJobKeys.HANDLER_CODE);
        String params = data.getString(QuartzJobKeys.PARAMS);
        boolean manual = data.getBooleanValue(QuartzJobKeys.MANUAL);
        try {
            jobHandlerRegistry.requireTask(handlerCode)
                    .execute(new JobContext(jobId, handlerCode, params, manual));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new org.quartz.JobExecutionException(e);
        }
    }

    private static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }
}
