package top.wyhao.starter.quartz.dispatch;

import org.quartz.DisallowConcurrentExecution;

/**
 * 禁止同一 JobDetail 重叠执行的分发器。
 */
@DisallowConcurrentExecution
public class DisallowConcurrentJobDispatcher extends AbstractJobDispatcher {
}
