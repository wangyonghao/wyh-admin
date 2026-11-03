package top.wyhao.system.auth.domain.user;

/**
 * 登录策略（重试、锁定、验证码阈值）。由应用层从配置组装后传入 domain 规则。
 */
public record PasswordPolicy(
        int maxRetry,
        int lockTimeMinutes,
        int captchaErrorThreshold
) {

    public static final int DEFAULT_MAX_RETRY = 5;
    public static final int DEFAULT_LOCK_MINUTES = 30;
    public static final int DEFAULT_CAPTCHA_THRESHOLD = 2;

    /**
     * @param captchaErrorThreshold -1 永不验证码；0 始终；1~N 失败达到阈值后要求
     */
    public PasswordPolicy {
        if (maxRetry < 1) {
            maxRetry = DEFAULT_MAX_RETRY;
        }
        if (lockTimeMinutes < 1) {
            lockTimeMinutes = DEFAULT_LOCK_MINUTES;
        }
    }

    public static PasswordPolicy of(Integer maxRetry, Integer lockTimeMinutes, Integer captchaErrorThreshold) {
        return new PasswordPolicy(
                maxRetry != null ? maxRetry : DEFAULT_MAX_RETRY,
                lockTimeMinutes != null ? lockTimeMinutes : DEFAULT_LOCK_MINUTES,
                captchaErrorThreshold != null ? captchaErrorThreshold : DEFAULT_CAPTCHA_THRESHOLD
        );
    }

    public boolean captchaFeatureEnabled() {
        return captchaErrorThreshold >= 0;
    }

    public boolean captchaAlwaysRequired() {
        return captchaErrorThreshold == 0;
    }
}
