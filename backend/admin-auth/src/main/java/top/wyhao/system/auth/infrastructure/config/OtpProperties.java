package top.wyhao.system.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OTP 配置属性（生成 / 限流）。模板与渠道开关由 System 的通知 API 持有。
 */
@Data
@Component
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

    /**
     * 验证码配置
     */
    private CodeConfig code = new CodeConfig();

    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    @Data
    public static class CodeConfig {
        /**
         * 验证码长度
         */
        private Integer length = 6;

        /**
         * 有效期（秒）
         */
        private Integer expiresIn = 300;
    }

    @Data
    public static class RateLimitConfig {
        private LimitRule global = new LimitRule(1000, 60);
        private LimitRule ip = new LimitRule(10, 3600);
        private LimitRule target = new LimitRule(3, 60);
        private Integer maxFail = 5;
    }

    @Data
    public static class LimitRule {
        private Integer max;
        private Integer window;

        public LimitRule() {
        }

        public LimitRule(Integer max, Integer window) {
            this.max = max;
            this.window = window;
        }
    }
}
