package top.wyhao.system.auth.infrastructure.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.settings.client.SmsConfigApi;
import top.wyhao.system.auth.domain.gateway.OtpDeliveryGateway;
import top.wyhao.system.auth.domain.otp.OtpChannel;
import top.wyhao.system.auth.domain.otp.OtpException;
import top.wyhao.system.auth.infrastructure.config.OtpProperties;
import top.wyhao.system.auth.infrastructure.otp.TemplateService;
import top.wyhao.system.cmn.mail.MailClient;
import top.wyhao.system.cmn.sms.SmsClient;

import java.util.LinkedHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpDeliveryGatewayImpl implements OtpDeliveryGateway {

    private final OtpProperties otpProperties;
    private final TemplateService templateService;
    private final MailClient mailClient;
    private final SmsClient smsClient;
    private final SmsConfigApi smsConfigApi;

    @Override
    public void sendEmail(String email, OtpScene scene, String locale, String code, int expireMinutes, String clientIp) {
        if (!isEmailEnabled()) {
            throw OtpException.sendFailed("邮件渠道未启用");
        }
        String content = templateService.render(
                OtpChannel.EMAIL, scene, locale, code, expireMinutes, email, clientIp);
        String subject = otpProperties.getChannel().getEmail().getSubjectPrefix() + scene.getDescription();
        try {
            mailClient.sendText(email, subject, content);
            log.info("OTP 邮件发送成功: target={}", email);
        } catch (Exception e) {
            log.error("OTP 邮件发送失败: target={}, error={}", email, e.getMessage(), e);
            throw OtpException.sendFailed("邮件发送失败: " + e.getMessage());
        }
    }

    @Override
    public void sendSms(String phone, OtpScene scene, String code, int expireMinutes) {
        if (!isSmsEnabled()) {
            throw OtpException.sendFailed("短信渠道未启用");
        }
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        params.put("expire", String.valueOf(expireMinutes));
        String templateId = smsConfigApi.getSmsTemplate(scene.name());
        boolean ok = smsClient.send(phone, templateId, params);
        if (!ok) {
            throw OtpException.sendFailed("短信发送失败");
        }
        log.info("OTP 短信发送成功: target={}", phone);
    }

    @Override
    public boolean isEmailEnabled() {
        return Boolean.TRUE.equals(otpProperties.getChannel().getEmail().getEnabled());
    }

    @Override
    public boolean isSmsEnabled() {
        return Boolean.TRUE.equals(otpProperties.getChannel().getSms().getEnabled());
    }
}
