
package top.wyhao.starter.core.autoconfigure.threadpool;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.ThreadPoolTaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.wyhao.starter.core.constant.PropertiesConstants;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link TaskScheduler}.
 *

 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(prefix = "spring.task.scheduling.extension", name = PropertiesConstants.ENABLED, havingValue = "true", matchIfMissing = true)
class TaskSchedulingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulingConfiguration.class);

    @Bean
    public ThreadPoolTaskSchedulerCustomizer threadPoolTaskSchedulerCustomizer(ThreadPoolExtensionProperties properties) {
        return executor -> executor.setRejectedExecutionHandler(properties.getScheduling()
            .getRejectedPolicy()
            .getRejectedExecutionHandler());
    }

    @PostConstruct
    public void postConstruct() {
        log.debug("[Tide Starter] - Auto Configuration 'TaskScheduler' completed initialization.");
    }
}
