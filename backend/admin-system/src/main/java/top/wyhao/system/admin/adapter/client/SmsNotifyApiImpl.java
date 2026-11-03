package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wyhao.notification.client.SmsNotifyApi;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.settings.client.SmsConfigApi;
import top.wyhao.system.admin.domain.exception.NotifyException;
import top.wyhao.system.admin.infrastructure.config.OtpNotifyProperties;
import top.wyhao.system.cmn.sms.SmsClient;

import java.util.LinkedHashMap;

/**
 * 短信通知：取场景模板 + SmsClient 投递。不生成验证码。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotifyApiImpl implements SmsNotifyApi {

    private final OtpNotifyProperties properties;
    private final SmsClient smsClient;
    private final SmsConfigApi smsConfigApi;

    @Override
    public void sendOtp(String phone, OtpScene scene, String code, int expireMinutes) {
        if (!isEnabled()) {
            throw NotifyException.channelDisabled("短信");
        }
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        params.put("expire", String.valueOf(expireMinutes));
        String templateId = smsConfigApi.getSmsTemplate(scene.name());
        boolean ok = smsClient.send(phone, templateId, params);
        if (!ok) {
            throw NotifyException.sendFailed("短信发送失败");
        }
        log.info("OTP 短信发送成功: target={}", phone);
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(properties.getChannel().getSms().getEnabled());
    }
}
