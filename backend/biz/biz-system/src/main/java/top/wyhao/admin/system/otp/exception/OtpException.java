package top.wyhao.admin.system.otp.exception;

import lombok.Getter;
import top.wyhao.starter.core.exception.BusinessException;

/**
 * OTP 自定义异常
 *
 * @author wyhao
 */
@Getter
public class OtpException extends BusinessException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * 重试时间（秒，可选）
     */
    private final Integer retryAfter;

    public OtpException(String code, String message) {
        this(code, message, null);
    }

    public OtpException(String code, String message, Integer retryAfter) {
        super(message);
        this.code = code;
        this.retryAfter = retryAfter;
    }

    /**
     * 验证码错误
     */
    public static OtpException invalid() {
        return new OtpException("OTP_INVALID", "验证码错误");
    }

    /**
     * 验证码已过期
     */
    public static OtpException expired() {
        return new OtpException("OTP_EXPIRED", "验证码已过期");
    }

    /**
     * 会话不存在
     */
    public static OtpException notFound() {
        return new OtpException("OTP_NOT_FOUND", "验证码会话不存在");
    }

    /**
     * 验证码已使用
     */
    public static OtpException alreadyUsed() {
        return new OtpException("OTP_ALREADY_USED", "验证码已使用");
    }

    /**
     * 请求过于频繁
     */
    public static OtpException rateLimitExceeded(int retryAfter) {
        return new OtpException("RATE_LIMIT_EXCEEDED", String.format("请求过于频繁，请 %d 秒后重试", retryAfter), retryAfter);
    }

    /**
     * 验证失败次数过多
     */
    public static OtpException locked() {
        return new OtpException("OTP_LOCKED", "验证失败次数过多，已锁定");
    }

    /**
     * 目标地址格式错误
     */
    public static OtpException invalidTarget(String message) {
        return new OtpException("INVALID_TARGET", message);
    }

    /**
     * 发送失败
     */
    public static OtpException sendFailed(String message) {
        return new OtpException("SEND_FAILED", message);
    }
}
