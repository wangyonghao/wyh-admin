package top.wyhao.notification.client;

import top.wyhao.security.client.otp.OtpScene;

/**
 * 短信通知 API（系统模块实现）。当前至少支持投递 OTP 短信。
 * 调用方传入已生成的 code，本 API 只负责取模板并调用短信通道。
 */
public interface SmsNotifyApi {

    /**
     * 发送 OTP 短信。{@code code} 由调用方生成并自行缓存；此处不得另行生成。
     *
     * @param phone         手机号
     * @param scene         业务场景
     * @param code          验证码明文
     * @param expireMinutes 有效期（分钟），写入模板参数
     */
    void sendOtp(String phone, OtpScene scene, String code, int expireMinutes);

    /**
     * 短信渠道是否启用。
     */
    boolean isEnabled();
}
