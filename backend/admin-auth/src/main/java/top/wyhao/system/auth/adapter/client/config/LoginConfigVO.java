package top.wyhao.system.auth.adapter.client.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 登录 / 认证相关配置。
 */
@Data
@Schema(description = "登录配置")
public class LoginConfigVO {

    /**
     * 兼容旧配置：是否开启验证码。新逻辑以 {@link #captchaErrorThreshold} 为准，
     * 读写时会与阈值同步（threshold &gt;= 0 视为开启）。
     */
    @Schema(description = "是否开启验证码（由 captchaErrorThreshold 派生，兼容旧客户端）", example = "true")
    private Boolean captchaEnabled;

    /**
     * 密码错误达到该次数后开启验证码。
     * <ul>
     *   <li>-1：永不开启验证码</li>
     *   <li>0：始终开启验证码</li>
     *   <li>1~5：错误累计达到该次数后开启</li>
     * </ul>
     */
    @Schema(description = "密码错误多少次后开启验证码：-1 不开启，0 始终开启，1~5 为阈值", example = "2")
    private Integer captchaErrorThreshold;

    @Schema(description = "验证码类型：graphic-图形验证码，behavior-行为验证码", example = "graphic")
    private String captchaType;

    @Schema(description = "登录最大重试次数", example = "5")
    private Integer maxRetry;

    @Schema(description = "登录锁定时间（分钟）", example = "30")
    private Integer lockTime;

    @Schema(description = "会话超时时间（分钟）", example = "30")
    private Integer sessionTimeout;

    /**
     * 解析有效的验证码错误阈值（含旧字段兼容）。
     */
    public int resolvedCaptchaErrorThreshold() {
        if (captchaErrorThreshold != null) {
            return captchaErrorThreshold;
        }
        if (Boolean.FALSE.equals(captchaEnabled)) {
            return -1;
        }
        return 2;
    }

    /**
     * 验证码功能是否可用（非 -1）。
     */
    public boolean isCaptchaFeatureEnabled() {
        return resolvedCaptchaErrorThreshold() >= 0;
    }

    /**
     * 是否始终要求验证码（阈值为 0）。
     */
    public boolean isCaptchaAlwaysRequired() {
        return resolvedCaptchaErrorThreshold() == 0;
    }

    /**
     * 失败次数是否已达到需要验证码的阈值。
     */
    public boolean needCaptcha(int retryCount) {
        int threshold = resolvedCaptchaErrorThreshold();
        if (threshold < 0) {
            return false;
        }
        return retryCount >= threshold;
    }

    /**
     * 读取配置后补齐默认值并同步派生字段。
     */
    public LoginConfigVO normalizeForRead() {
        if (captchaType == null || captchaType.isBlank()) {
            captchaType = "graphic";
        }
        if (maxRetry == null) {
            maxRetry = 5;
        }
        if (lockTime == null) {
            lockTime = 30;
        }
        if (sessionTimeout == null) {
            sessionTimeout = 30;
        }
        captchaErrorThreshold = resolvedCaptchaErrorThreshold();
        captchaEnabled = captchaErrorThreshold >= 0;
        return this;
    }

    /**
     * 保存前校验并规范化。
     */
    public void normalizeAndValidateForWrite() {
        if (captchaErrorThreshold == null) {
            captchaErrorThreshold = Boolean.FALSE.equals(captchaEnabled) ? -1 : 2;
        }
        int threshold = captchaErrorThreshold;
        if (threshold != -1 && (threshold < 0 || threshold > 5)) {
            throw BizException.of("LOGIN_CONFIG_INVALID", "验证码开启阈值须为 -1 或 0~5");
        }
        if (maxRetry == null) {
            maxRetry = 5;
        }
        if (threshold >= 0 && threshold >= maxRetry) {
            throw BizException.of("LOGIN_CONFIG_INVALID", "验证码开启阈值须小于最大重试次数");
        }
        if (captchaType == null || captchaType.isBlank()) {
            captchaType = "graphic";
        }
        if (lockTime == null || lockTime < 1) {
            lockTime = 30;
        }
        if (sessionTimeout == null || sessionTimeout < 5) {
            sessionTimeout = 30;
        }
        captchaEnabled = threshold >= 0;
    }
}
