
package top.wyhao.web.ratelimit;

import top.wyhao.cmn.core.exception.SystemException;

/**
 * 限流异常
 */
public class RateLimiterException extends SystemException {

    public RateLimiterException(String message) {
        super(message);
    }

    public RateLimiterException(String message, Throwable cause) {
        super(message, cause);
    }
}
