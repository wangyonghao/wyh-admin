package top.wyhao.admin.system.otp.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送 OTP 验证码响应
 *
 * @author wyhao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送 OTP 验证码响应")
public class OtpSendResult {

    /**
     * OTP 会话 UUID
     */
    @Schema(description = "OTP 会话 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String otpUuid;

    /**
     * 验证码有效期（秒）
     */
    @Schema(description = "验证码有效期（秒）", example = "300")
    private Integer expiresIn;

    /**
     * 提示信息
     */
    @Schema(description = "提示信息", example = "验证码已发送")
    private String message;
}
