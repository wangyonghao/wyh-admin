package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证 OTP 验证码响应 Record
 */
@Schema(description = "验证 OTP 验证码响应")

public record OtpVerifyResponse(
        /**
         * 验证结果
         */
        @Schema(description = "验证结果", example = "true")
        Boolean verified,

        /**
         * 提示信息
         */
        @Schema(description = "提示信息", example = "验证成功")
        String message
) {}
