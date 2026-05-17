package top.wyhao.admin.system.otp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OTP 配置属性
 *
 * @author wyhao
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

    /**
     * 模板配置
     */
    private TemplateConfig template = new TemplateConfig();

    /**
     * 渠道配置
     */
    private ChannelConfig channel = new ChannelConfig();

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
        /**
         * 全局限流
         */
        private LimitRule global = new LimitRule(1000, 60);

        /**
         * IP 限流
         */
        private LimitRule ip = new LimitRule(10, 3600);

        /**
         * 目标地址限流
         */
        private LimitRule target = new LimitRule(3, 60);

        /**
         * 最大失败次数
         */
        private Integer maxFail = 5;
    }

    @Data
    public static class LimitRule {
        /**
         * 最大次数
         */
        private Integer max;

        /**
         * 时间窗口（秒）
         */
        private Integer window;

        public LimitRule() {
        }

        public LimitRule(Integer max, Integer window) {
            this.max = max;
            this.window = window;
        }
    }

    @Data
    public static class TemplateConfig {
        /**
         * 模板基础路径
         */
        private String basePath = "templates/otp";

        /**
         * 默认语言
         */
        private String defaultLocale = "zh_CN";
    }

    @Data
    public static class ChannelConfig {
        /**
         * 邮件配置
         */
        private EmailConfig email = new EmailConfig();

        /**
         * 短信配置
         */
        private SmsConfig sms = new SmsConfig();
    }

    @Data
    public static class EmailConfig {
        /**
         * 是否启用
         */
        private Boolean enabled = true;

        /**
         * 发件人
         */
        private String from = "noreply@example.com";

        /**
         * 主题前缀
         */
        private String subjectPrefix = "【WYH Admin】";
    }

    @Data
    public static class SmsConfig {
        /**
         * 是否启用
         */
        private Boolean enabled = true;

        /**
         * 服务商
         */
        private String provider = "aliyun";

        /**
         * 短信签名
         */
        private String signName = "WYH Admin";
    }
}
