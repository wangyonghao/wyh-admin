package top.wyhao.admin.system.otp.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证 OTP 验证码响应
 *
 * @author wyhao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证 OTP 验证码响应")
public class OtpVerifyResult {

    /**
     * 验证结果
     */
    @Schema(description = "验证结果", example = "true")
    private Boolean verified;

    /**
     * 提示信息
     */
    @Schema(description = "提示信息", example = "验证成功")
    private String message;
}
