package top.wyhao.starter.quartz;

import org.quartz.*;
import top.wyhao.cmn.core.exception.BizException;
import top.wyhao.starter.quartz.dispatch.ConcurrentJobDispatcher;
import top.wyhao.starter.quartz.dispatch.DisallowConcurrentJobDispatcher;
import top.wyhao.starter.quartz.spi.JobHandlerDescriptor;

import java.time.Instant;
import java.util.TimeZone;

/**
 * 对 Scheduler 的业务封装：按 sys_job.id 创建 / 暂停 / 触发。
 */
public class QuartzJobScheduler {

    private final Scheduler scheduler;
    private final JobHandlerRegistry jobHandlerRegistry;

    public QuartzJobScheduler(Scheduler scheduler, JobHandlerRegistry jobHandlerRegistry) {
        this.scheduler = scheduler;
        this.jobHandlerRegistry = jobHandlerRegistry;
    }

    public void schedule(Long jobId, String handlerCode, String cron, String params, boolean enabled) {
        try {
            JobDetail jobDetail = buildJobDetail(jobId, handlerCode, params);
            CronTrigger trigger = buildTrigger(jobId, cron);
            scheduler.scheduleJob(jobDetail, trigger);
            if (!enabled) {
                scheduler.pauseJob(QuartzJobKeys.jobKey(jobId));
            }
        } catch (SchedulerException e) {
            throw new BizException("调度任务失败: " + e.getMessage());
        }
    }

    public void reschedule(Long jobId, String handlerCode, String cron, String params, boolean enabled) {
        try {
            JobKey jobKey = QuartzJobKeys.jobKey(jobId);
            TriggerKey triggerKey = QuartzJobKeys.triggerKey(jobId);
            CronTrigger trigger = buildTrigger(jobId, cron);
            if (scheduler.checkExists(jobKey)) {
                scheduler.addJob(buildJobDetail(jobId, handlerCode, params), true, true);
                if (scheduler.rescheduleJob(triggerKey, trigger) == null) {
                    scheduler.scheduleJob(trigger);
                }
            } else {
                scheduler.scheduleJob(buildJobDetail(jobId, handlerCode, params), trigger);
            }
            if (enabled) {
                scheduler.resumeJob(jobKey);
            } else {
                scheduler.pauseJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new BizException("更新调度失败: " + e.getMessage());
        }
    }

    public void pause(Long jobId) {
        try {
            JobKey jobKey = QuartzJobKeys.jobKey(jobId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new BizException("停止任务失败: " + e.getMessage());
        }
    }

    public void resume(Long jobId) {
        try {
            JobKey jobKey = QuartzJobKeys.jobKey(jobId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.resumeJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new BizException("激活任务失败: " + e.getMessage());
        }
    }

    public void delete(Long jobId) {
        try {
            JobKey jobKey = QuartzJobKeys.jobKey(jobId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new BizException("删除调度失败: " + e.getMessage());
        }
    }

    public void triggerOnce(Long jobId) {
        try {
            JobKey jobKey = QuartzJobKeys.jobKey(jobId);
            if (!scheduler.checkExists(jobKey)) {
                throw new BizException("任务尚未注册到调度器");
            }
            JobDataMap data = new JobDataMap();
            data.put(QuartzJobKeys.MANUAL, true);
            scheduler.triggerJob(jobKey, data);
        } catch (SchedulerException e) {
            throw new BizException("立即执行失败: " + e.getMessage());
        }
    }

    public Instant nextFireTime(Long jobId) {
        try {
            Trigger trigger = scheduler.getTrigger(QuartzJobKeys.triggerKey(jobId));
            if (trigger == null || trigger.getNextFireTime() == null) {
                return null;
            }
            return trigger.getNextFireTime().toInstant();
        } catch (SchedulerException e) {
            return null;
        }
    }

    public boolean exists(Long jobId) {
        try {
            return scheduler.checkExists(QuartzJobKeys.jobKey(jobId));
        } catch (SchedulerException e) {
            return false;
        }
    }

    private JobDetail buildJobDetail(Long jobId, String handlerCode, String params) {
        JobHandlerDescriptor descriptor = jobHandlerRegistry.requireDescriptor(handlerCode);
        Class<? extends Job> jobClass = descriptor.isAllowConcurrent()
                ? ConcurrentJobDispatcher.class
                : DisallowConcurrentJobDispatcher.class;
        return JobBuilder.newJob(jobClass)
                .withIdentity(QuartzJobKeys.jobKey(jobId))
                .usingJobData(QuartzJobKeys.JOB_ID, jobId)
                .usingJobData(QuartzJobKeys.HANDLER_CODE, handlerCode)
                .usingJobData(QuartzJobKeys.PARAMS, params == null ? "" : params)
                .usingJobData(QuartzJobKeys.MANUAL, false)
                .storeDurably()
                .requestRecovery()
                .build();
    }

    private CronTrigger buildTrigger(Long jobId, String cron) {
        return TriggerBuilder.newTrigger()
                .withIdentity(QuartzJobKeys.triggerKey(jobId))
                .forJob(QuartzJobKeys.jobKey(jobId))
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                        .inTimeZone(TimeZone.getDefault())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }
}
