package top.wyhao.admin.system.otp.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 验证 OTP 验证码请求
 *

 */
@Data
@Schema(description = "验证 OTP 验证码请求")
public class OtpVerifyRequest {

    /**
     * OTP 会话 UUID
     */
    @Schema(description = "OTP 会话 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "OTP UUID 不能为空")
    private String otpUuid;

    /**
     * 验证码
     */
    @Schema(description = "验证码（6位数字）", example = "123456")
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式错误")
    private String code;
}
