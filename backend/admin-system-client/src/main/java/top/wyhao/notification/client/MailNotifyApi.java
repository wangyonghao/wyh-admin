package top.wyhao.notification.client;

import top.wyhao.security.client.otp.OtpScene;

/**
 * 邮件通知 API（系统模块实现）。当前至少支持投递 OTP 邮件。
 * 调用方传入已生成的 code，本 API 只负责模板渲染与发送。
 */
public interface MailNotifyApi {

    /**
     * 发送 OTP 邮件。{@code code} 由调用方生成并自行缓存；此处不得另行生成。
     *
     * @param email         收件邮箱
     * @param scene         业务场景
     * @param locale        模板语言，可空（走默认）
     * @param code          验证码明文
     * @param expireMinutes 有效期（分钟），用于文案
     * @param clientIp      请求 IP，可空
     */
    void sendOtp(String email, OtpScene scene, String locale, String code, int expireMinutes, String clientIp);

    /**
     * 邮件渠道是否启用。
     */
    boolean isEnabled();
}
