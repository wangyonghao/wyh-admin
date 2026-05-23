
package top.wyhao.starter.tenant.annotation;

import java.lang.annotation.*;

/**
 * 是否启用租户判断注解
 *

 * @since 2.13.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented

public @interface ConditionalOnEnabledTenant {
}