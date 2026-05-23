
package top.wyhao.admin.schedule.model.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import top.wyhao.starter.core.constant.PropertiesConstants;

import java.lang.annotation.*;

/**
 * 是否启用 Snail Job 判断注解

 * @since 2025/5/18 12:03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnProperty(prefix = "snail-job", name = PropertiesConstants.ENABLED, havingValue = "true", matchIfMissing = true)
public @interface ConditionalOnEnabledScheduleJob {
}