
package top.wyhao.starter.core.autoconfigure.threadpool;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

/**
 * 线程池自动配置
 *

 * @since 1.0.0
 */
@Lazy
@AutoConfiguration
@EnableConfigurationProperties(ThreadPoolExtensionProperties.class)
@Import({TaskExecutionConfiguration.class, TaskSchedulingConfiguration.class})
public class ThreadPoolAutoConfiguration {
}
