package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 邮箱修改请求参数
 */
@Data
@Schema(description = "邮箱修改请求参数")
public class ProfileEmailUpdateRequest {

    @Schema(description = "新邮箱", example = "123456789@qq.com")
    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "新邮箱格式不正确")
    private String email;

    @Schema(description = "验证码", example = "888888")
    @NotBlank(message = "验证码不能为空")
    @Length(max = 6, message = "验证码无效")
    private String captcha;

    @Schema(description = "当前密码", example = "RSA 公钥加密的当前密码")
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;
}
