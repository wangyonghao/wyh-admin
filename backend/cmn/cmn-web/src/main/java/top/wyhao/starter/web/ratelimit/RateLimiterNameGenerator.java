
package top.wyhao.starter.web.ratelimit;

import java.lang.reflect.Method;

/**
 * 限流器名称生成器
 *

 * @since 2.2.0
 */
public interface RateLimiterNameGenerator {

    /**
     * Generate a rate limiter name for the given method and its parameters.
     *
     * @param target the target instance
     * @param method the method being called
     * @param args   the method parameters (with any var-args expanded)
     * @return a generated rate limiter name
     */
    String generate(Object target, Method method, Object... args);
}
