package top.wyhao.admin.system.otp.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.wyhao.admin.system.otp.enums.OtpChannel;
import top.wyhao.admin.system.otp.enums.OtpScene;

/**
 * 发送 OTP 验证码请求
 *
 * @author wyhao
 */
@Data
@Schema(description = "发送 OTP 验证码请求")
public class OtpSendRequest {

    /**
     * 发送渠道
     */
    @Schema(description = "发送渠道（EMAIL-邮件，SMS-短信）", example = "EMAIL")
    @NotNull(message = "发送渠道不能为空")
    private OtpChannel channel;

    /**
     * 业务场景
     */
    @Schema(description = "业务场景", example = "REGISTER")
    @NotNull(message = "业务场景不能为空")
    private OtpScene scene;

    /**
     * 目标地址（邮箱或手机号）
     */
    @Schema(description = "目标地址（邮箱或手机号）", example = "user@example.com")
    @NotBlank(message = "目标地址不能为空")
    private String target;

    /**
     * 语言代码
     */
    @Schema(description = "语言代码", example = "zh_CN")
    private String locale;
}
