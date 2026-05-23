
package top.wyhao.admin.system.model.bo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户邮箱修改请求参数
 *

 * @since 2023/1/12 20:18
 */
@Data
@Schema(description = "用户邮箱修改请求参数")
public class UserEmailUpdateRequest {
    /**
     * 新邮箱
     */
    @Schema(description = "新邮箱", example = "123456789@qq.com")
    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "新邮箱格式不正确")
    private String email;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "888888")
    @NotBlank(message = "验证码不能为空")
    @Length(max = 6, message = "验证码无效")
    private String captcha;

    /**
     * 当前密码
     */
    @Schema(description = "当前密码", example = "RSA 公钥加密的当前密码")
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;
}
