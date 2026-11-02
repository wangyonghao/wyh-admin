package top.wyhao.system.auth.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.auth.domain.otp.OtpDomainService;

/**
 * 修改手机 / 邮箱时校验联系方式验证码：委托 OTP 域（与发码同一套缓存）。
 * 发码请走 {@code POST /auth/otp}，场景 {@code CHANGE_PHONE} / {@code CHANGE_EMAIL}。
 */
@Service
@RequiredArgsConstructor
public class ContactCaptchaApiImpl implements ContactCaptchaApi {

    private final OtpDomainService otpDomainService;

    @Override
    public void verifyPhone(String phone, String captcha) {
        otpDomainService.verify(phone, captcha, null);
    }

    @Override
    public void verifyEmail(String email, String captcha) {
        otpDomainService.verify(email, captcha, null);
    }
}
