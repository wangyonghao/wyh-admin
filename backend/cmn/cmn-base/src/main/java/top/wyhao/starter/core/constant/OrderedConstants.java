
package top.wyhao.starter.core.constant;

import org.springframework.core.Ordered;

/**
 * 过滤器和拦截器相关顺序常量
 *

 * @since 2.13.3
 */
public class OrderedConstants {

    /**
     * 过滤器顺序
     */
    public static final class Filter {

        /**
         * API 加密过滤器顺序
         */
        public static final int API_ENCRYPT_FILTER = Ordered.HIGHEST_PRECEDENCE;

        /**
         * 链路追踪过滤器顺序
         */
        public static final int TRACE_FILTER = Ordered.HIGHEST_PRECEDENCE + 100;

        /**
         * 日志过滤器顺序
         */
        public static final int LOG_FILTER = Ordered.LOWEST_PRECEDENCE - 100;

        private Filter() {
        }
    }

    /**
     * 拦截器顺序
     */
    public static final class Interceptor {

        /**
         * 租户拦截器顺序
         */
        public static final int TENANT_INTERCEPTOR = Ordered.HIGHEST_PRECEDENCE + 100;

        /**
         * 认证拦截器顺序
         */
        public static final int AUTH_INTERCEPTOR = Ordered.HIGHEST_PRECEDENCE + 200;

        /**
         * 日志拦截器顺序
         */
        public static final int LOG_INTERCEPTOR = Ordered.LOWEST_PRECEDENCE - 100;

        private Interceptor() {
        }
    }

    private OrderedConstants() {
    }
}