
package top.wyhao.starter.core.autoconfigure.threadpool;

import cn.hutool.core.util.ArrayUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.wyhao.starter.core.constant.PropertiesConstants;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.exception.SystemException;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link TaskExecutor}.
 *

 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "spring.task.execution.extension", name = PropertiesConstants.ENABLED, havingValue = "true", matchIfMissing = true)
class TaskExecutionConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutionConfiguration.class);

    @Value("${spring.task.execution.pool.core-size:#{T(java.lang.Runtime).getRuntime().availableProcessors() + 1}}")
    private int corePoolSize;

    @Value("${spring.task.execution.pool.max-size:#{T(java.lang.Runtime).getRuntime().availableProcessors() * 2}}")
    private int maxPoolSize;

    @Bean
    public ThreadPoolTaskExecutorCustomizer threadPoolTaskExecutorCustomizer(ThreadPoolExtensionProperties properties) {
        return executor -> {
            // 核心（最小）线程数
            executor.setCorePoolSize(corePoolSize);
            // 最大线程数
            executor.setMaxPoolSize(maxPoolSize);
            // 当线程池的任务缓存队列已满并且线程池中的线程数已达到 maxPoolSize 时采取的任务拒绝策略
            executor.setRejectedExecutionHandler(properties.getExecution()
                .getRejectedPolicy()
                .getRejectedExecutionHandler());
        };
    }

    /**
     * {@link Async} 异步任务线程池配置
     */
    @EnableAsync(proxyTargetClass = true)
    static class AsyncThreadPoolConfigurer implements AsyncConfigurer {

        private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

        public AsyncThreadPoolConfigurer(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
            this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        }

        @Override
        public Executor getAsyncExecutor() {
            return threadPoolTaskExecutor;
        }

        /**
         * 异步任务执行时的异常处理
         */
        @Override
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
            return (throwable, method, objects) -> {
                StringBuilder sb = new StringBuilder();
                sb.append("Exception message: ")
                    .append(throwable.getMessage())
                    .append(", Method name: ")
                    .append(method.getName());
                if (ArrayUtil.isNotEmpty(objects)) {
                    sb.append(", Parameter value: ").append(Arrays.toString(objects));
                }
                throw new SystemException(sb.toString());
            };
        }

        @PostConstruct
        public void postConstruct() {
            log.debug("[Tide Starter] - Auto Configuration 'TaskExecutor-@Async' completed initialization.");
        }
    }

    @PostConstruct
    public void postConstruct() {
        log.debug("[Tide Starter] - Auto Configuration 'TaskExecutor' completed initialization.");
    }
}
