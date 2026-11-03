package top.wyhao.system.auth.domain.gateway;

import top.wyhao.security.client.otp.OtpScene;
/**
 * OTP 投递（邮件 / 短信）。渲染模板与具体发送在基础设施实现。
 */
public interface OtpDeliveryGateway {

    /**
     * 发送邮件 OTP：模板渲染 + 投递。{@code expireMinutes} 仅用于文案展示。
     */
    void sendEmail(String email, OtpScene scene, String locale, String code, int expireMinutes, String clientIp);

    /**
     * 发送短信 OTP：使用同一份 {@code code}，不得另行生成。
     */
    void sendSms(String phone, OtpScene scene, String code, int expireMinutes);

    /**
     * 邮件渠道是否启用。
     */
    boolean isEmailEnabled();

    /**
     * 短信渠道是否启用。
     */
    boolean isSmsEnabled();
}
