package top.wyhao.starter.quartz.spi;

import java.time.Instant;

/**
 * 任务执行记录。由管理模块实现并写入业务日志表。
 */
public interface JobExecutionRecorder {

    void onStart(JobExecutionRecord record);

    void onFinish(JobExecutionRecord record);

    class JobExecutionRecord {
        private Long jobId;
        private String handlerCode;
        private boolean manual;
        private Instant startTime;
        private Instant endTime;
        private boolean success;
        private String errorMessage;

        public Long getJobId() {
            return jobId;
        }

        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }

        public String getHandlerCode() {
            return handlerCode;
        }

        public void setHandlerCode(String handlerCode) {
            this.handlerCode = handlerCode;
        }

        public boolean isManual() {
            return manual;
        }

        public void setManual(boolean manual) {
            this.manual = manual;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public void setStartTime(Instant startTime) {
            this.startTime = startTime;
        }

        public Instant getEndTime() {
            return endTime;
        }

        public void setEndTime(Instant endTime) {
            this.endTime = endTime;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
