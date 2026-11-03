package top.wyhao.starter.quartz.spi;

/**
 * 任务执行上下文
 */
public class JobContext {

    private final Long jobId;
    private final String handlerCode;
    private final String params;
    private final boolean manual;

    public JobContext(Long jobId, String handlerCode, String params, boolean manual) {
        this.jobId = jobId;
        this.handlerCode = handlerCode;
        this.params = params;
        this.manual = manual;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getHandlerCode() {
        return handlerCode;
    }

    public String getParams() {
        return params;
    }

    public boolean isManual() {
        return manual;
    }
}
