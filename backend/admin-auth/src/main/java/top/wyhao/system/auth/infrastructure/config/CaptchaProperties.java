package top.wyhao.system.auth.infrastructure.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 图形验证码配置（联系方式验证码走 otp.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    @Value("${wyhao-starter.captcha.graphic.expirationInSeconds}")
    private long expirationInSeconds;
}
