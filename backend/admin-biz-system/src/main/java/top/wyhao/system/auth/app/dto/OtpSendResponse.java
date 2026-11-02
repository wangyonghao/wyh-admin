package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 发送 OTP 验证码响应 Record
 */
@Schema(description = "发送 OTP 验证码响应")
public record OtpSendResponse(
        /**
         * 验证码有效期（秒）
         */
        @Schema(description = "验证码有效期（秒）", example = "300")
        Integer expiresIn,

        /**
         * 提示信息
         */
        @Schema(description = "提示信息", example = "验证码已发送")
        String message,

        /**
         * OTP 会话 UUID，校验时回传
         */
        @Schema(description = "OTP 会话 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        String otpUuid
) {}
