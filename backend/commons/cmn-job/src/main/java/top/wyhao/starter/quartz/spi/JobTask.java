package top.wyhao.starter.quartz.spi;

/**
 * 可调度任务。由业务模块实现，内核按 {@code @JobHandler.code} 分发。
 */
@FunctionalInterface
public interface JobTask {

    void execute(JobContext context) throws Exception;
}
