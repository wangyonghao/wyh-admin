package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.wyhao.web.validation.Mobile;

/**
 * 手机号修改请求参数
 */
@Data
@Schema(description = "手机号修改请求参数")
public class ProfilePhoneUpdateRequest {

    @Schema(description = "新手机号", example = "13811111111")
    @NotBlank(message = "新手机号不能为空")
    @Mobile(message = "新手机号格式不正确")
    private String phone;

    @Schema(description = "验证码", example = "888888")
    @NotBlank(message = "验证码不能为空")
    @Length(max = 6, message = "验证码无效")
    private String captcha;

    @Schema(description = "当前密码", example = "RSA 公钥加密的当前密码")
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;
}
