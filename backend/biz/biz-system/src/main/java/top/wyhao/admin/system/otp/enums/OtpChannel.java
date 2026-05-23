package top.wyhao.admin.system.otp.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OTP 发送渠道枚举
 *

 */
@Getter
@RequiredArgsConstructor
public enum OtpChannel {

    /**
     * 邮件
     */
    EMAIL("邮件"),

    /**
     * 短信
     */
    SMS("短信");

    private final String description;
}
