package top.wyhao.system.admin.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.system.admin.domain.gateway.JobLogRepository;
import top.wyhao.system.admin.domain.gateway.JobRepository;
import top.wyhao.system.admin.domain.model.Job;
import top.wyhao.system.admin.domain.model.JobLog;
import top.wyhao.starter.quartz.spi.JobExecutionRecorder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class JobLogRecorder implements JobExecutionRecorder {

    public static final int RUNNING = 1;
    public static final int SUCCESS = 2;
    public static final int FAILURE = 3;

    private final JobLogRepository jobLogRepository;
    private final JobRepository jobRepository;

    @Override
    public void onStart(JobExecutionRecord record) {
        JobLog log = new JobLog();
        log.setJobId(record.getJobId());
        log.setJobName(resolveJobName(record.getJobId()));
        log.setHandlerCode(record.getHandlerCode());
        log.setTriggerType(record.isManual() ? "MANUAL" : "CRON");
        log.setStartTime(toLocal(record.getStartTime()));
        log.setStatus(RUNNING);
        jobLogRepository.insert(log);
    }

    @Override
    public void onFinish(JobExecutionRecord record) {
        JobLog running = jobLogRepository.findLatestRunning(record.getJobId(), RUNNING);
        LocalDateTime end = toLocal(record.getEndTime());
        if (running == null) {
            running = new JobLog();
            running.setJobId(record.getJobId());
            running.setJobName(resolveJobName(record.getJobId()));
            running.setHandlerCode(record.getHandlerCode());
            running.setTriggerType(record.isManual() ? "MANUAL" : "CRON");
            running.setStartTime(toLocal(record.getStartTime()));
            running.setEndTime(end);
            running.setDurationMs(duration(running.getStartTime(), end));
            running.setStatus(record.isSuccess() ? SUCCESS : FAILURE);
            running.setErrorMessage(truncate(record.getErrorMessage()));
            jobLogRepository.insert(running);
            return;
        }
        running.setEndTime(end);
        running.setDurationMs(duration(running.getStartTime(), end));
        running.setStatus(record.isSuccess() ? SUCCESS : FAILURE);
        running.setErrorMessage(truncate(record.getErrorMessage()));
        jobLogRepository.updateById(running);
    }

    private String resolveJobName(Long jobId) {
        if (jobId == null) {
            return null;
        }
        Job job = jobRepository.findById(jobId);
        return job == null ? null : job.getName();
    }

    private static LocalDateTime toLocal(java.time.Instant instant) {
        if (instant == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static Long duration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        return ChronoUnit.MILLIS.between(start, end);
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }
}
