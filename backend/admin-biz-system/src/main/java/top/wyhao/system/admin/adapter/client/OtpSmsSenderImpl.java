package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.security.client.otp.OtpSmsSender;
import top.wyhao.system.admin.app.service.SmsService;

/**
 * OTP 短信发送适配
 */
@Service
@RequiredArgsConstructor
public class OtpSmsSenderImpl implements OtpSmsSender {

    private final SmsService smsService;

    @Override
    public void sendOtp(String phone, OtpScene scene) {
        smsService.sendOtp(phone, scene);
    }
}
