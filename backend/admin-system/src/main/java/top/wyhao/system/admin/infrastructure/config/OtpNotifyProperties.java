package top.wyhao.system.admin.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OTP 通知投递配置（模板 + 渠道）。与 auth 的 {@code otp.code}/{@code otp.rate-limit} 共用前缀。
 */
@Data
@Component
@ConfigurationProperties(prefix = "otp")
public class OtpNotifyProperties {

    private TemplateConfig template = new TemplateConfig();
    private ChannelConfig channel = new ChannelConfig();

    @Data
    public static class TemplateConfig {
        private String basePath = "templates/otp";
        private String defaultLocale = "zh_CN";
    }

    @Data
    public static class ChannelConfig {
        private EmailConfig email = new EmailConfig();
        private SmsConfig sms = new SmsConfig();
    }

    @Data
    public static class EmailConfig {
        private Boolean enabled = true;
        private String from = "noreply@example.com";
        private String subjectPrefix = "【WYH Admin】";
    }

    @Data
    public static class SmsConfig {
        private Boolean enabled = true;
        private String provider = "aliyun";
        private String signName = "WYH Admin";
    }
}
