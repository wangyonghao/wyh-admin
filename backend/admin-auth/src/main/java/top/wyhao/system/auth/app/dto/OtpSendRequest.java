package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.web.validation.EnumValue;

import java.io.Serial;

/**
 * 发送 OTP 验证码请求 Record
 */
@Schema(description = "发送 OTP 验证码请求")
public record OtpSendRequest(

        /**
         * 业务场景
         */
        @Schema(description = "业务场景", example = "REGISTER")
        @NotNull(message = "业务场景不能为空")
        OtpScene scene,
        String email,
        String phone,

        /**
         * 语言代码
         */
        @Schema(description = "语言代码", example = "zh_CN")
        String locale
) {
    @NotNull(message = "请提供邮箱或手机")
    public String identifier() {
        return email != null ? email : phone;
    }
}