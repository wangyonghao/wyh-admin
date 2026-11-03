package top.wyhao.starter.quartz;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.ObjectProvider;
import top.wyhao.starter.quartz.spi.JobExecutionRecorder;
import top.wyhao.starter.quartz.spi.JobExecutionRecorder.JobExecutionRecord;

import java.time.Instant;

/**
 * 把每次执行结果交给可选的 {@link JobExecutionRecorder}。
 */
public class JobExecutionListener implements JobListener {

    private final ObjectProvider<JobExecutionRecorder> recorder;

    public JobExecutionListener(ObjectProvider<JobExecutionRecorder> recorder) {
        this.recorder = recorder;
    }

    @Override
    public String getName() {
        return "wyhJobExecutionListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobExecutionRecorder writer = recorder.getIfAvailable();
        if (writer == null) {
            return;
        }
        long start = System.currentTimeMillis();
        context.getJobDetail().getJobDataMap().put(QuartzJobKeys.START_EPOCH_MS, start);
        JobExecutionRecord record = baseRecord(context);
        record.setStartTime(Instant.ofEpochMilli(start));
        writer.onStart(record);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // 无否决逻辑
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobExecutionRecorder writer = recorder.getIfAvailable();
        if (writer == null) {
            return;
        }
        JobDataMap data = context.getJobDetail().getJobDataMap();
        long start = data.getLongValue(QuartzJobKeys.START_EPOCH_MS);
        if (start <= 0) {
            start = System.currentTimeMillis();
        }
        JobExecutionRecord record = baseRecord(context);
        record.setStartTime(Instant.ofEpochMilli(start));
        record.setEndTime(Instant.now());
        record.setSuccess(jobException == null);
        if (jobException != null) {
            Throwable cause = jobException.getCause() != null ? jobException.getCause() : jobException;
            record.setErrorMessage(cause.getMessage());
        }
        writer.onFinish(record);
    }

    private static JobExecutionRecord baseRecord(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        JobExecutionRecord record = new JobExecutionRecord();
        Object jobId = data.get(QuartzJobKeys.JOB_ID);
        if (jobId instanceof Number number) {
            record.setJobId(number.longValue());
        } else if (jobId != null) {
            record.setJobId(Long.valueOf(String.valueOf(jobId)));
        }
        record.setHandlerCode(data.getString(QuartzJobKeys.HANDLER_CODE));
        record.setManual(data.getBooleanValue(QuartzJobKeys.MANUAL));
        return record;
    }
}
