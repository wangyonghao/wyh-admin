package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wyhao.notification.client.MailNotifyApi;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.system.admin.domain.exception.NotifyException;
import top.wyhao.system.admin.infrastructure.config.OtpNotifyProperties;
import top.wyhao.system.admin.infrastructure.notify.OtpTemplateService;
import top.wyhao.system.cmn.mail.MailClient;

/**
 * 邮件通知：模板渲染 + MailClient 投递。不生成验证码。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailNotifyApiImpl implements MailNotifyApi {

    private final OtpNotifyProperties properties;
    private final OtpTemplateService templateService;
    private final MailClient mailClient;

    @Override
    public void sendOtp(String email, OtpScene scene, String locale, String code, int expireMinutes, String clientIp) {
        if (!isEnabled()) {
            throw NotifyException.channelDisabled("邮件");
        }
        String content = templateService.render(
                OtpTemplateService.Channel.EMAIL, scene, locale, code, expireMinutes, email, clientIp);
        String subject = properties.getChannel().getEmail().getSubjectPrefix() + scene.getDescription();
        try {
            mailClient.sendText(email, subject, content);
            log.info("OTP 邮件发送成功: target={}", email);
        } catch (Exception e) {
            log.error("OTP 邮件发送失败: target={}, error={}", email, e.getMessage(), e);
            throw NotifyException.sendFailed("邮件发送失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(properties.getChannel().getEmail().getEnabled());
    }
}
