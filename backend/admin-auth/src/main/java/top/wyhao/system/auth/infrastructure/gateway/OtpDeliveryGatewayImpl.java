package top.wyhao.system.auth.infrastructure.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.notification.client.MailNotifyApi;
import top.wyhao.notification.client.SmsNotifyApi;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.system.auth.domain.gateway.OtpDeliveryGateway;
import top.wyhao.system.auth.domain.otp.OtpException;

/**
 * OTP 投递网关：委托 System 的 MailNotifyApi / SmsNotifyApi，不直连 Mail/Sms 实现。
 */
@Component
@RequiredArgsConstructor
public class OtpDeliveryGatewayImpl implements OtpDeliveryGateway {

    private final MailNotifyApi mailNotifyApi;
    private final SmsNotifyApi smsNotifyApi;

    @Override
    public void sendEmail(String email, OtpScene scene, String locale, String code, int expireMinutes, String clientIp) {
        if (!isEmailEnabled()) {
            throw OtpException.sendFailed("邮件渠道未启用");
        }
        try {
            mailNotifyApi.sendOtp(email, scene, locale, code, expireMinutes, clientIp);
        } catch (OtpException e) {
            throw e;
        } catch (Exception e) {
            throw OtpException.sendFailed(e.getMessage() != null ? e.getMessage() : "邮件发送失败");
        }
    }
    // 验证码的生成、缓存、校验、限流仍留在 auth
    @Override
    public void sendSms(String phone, OtpScene scene, String code, int expireMinutes) {
        if (!isSmsEnabled()) {
            throw OtpException.sendFailed("短信渠道未启用");
        }
        try {

            smsNotifyApi.sendOtp(phone, scene, code, expireMinutes);
        } catch (OtpException e) {
            throw e;
        } catch (Exception e) {
            throw OtpException.sendFailed(e.getMessage() != null ? e.getMessage() : "短信发送失败");
        }
    }

    @Override
    public boolean isEmailEnabled() {
        return mailNotifyApi.isEnabled();
    }

    @Override
    public boolean isSmsEnabled() {
        return smsNotifyApi.isEnabled();
    }
}
