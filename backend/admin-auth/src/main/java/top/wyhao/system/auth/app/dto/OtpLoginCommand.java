package top.wyhao.system.auth.app.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import top.wyhao.system.auth.domain.model.ClientContext;

/**
 * 邮箱 / 手机验证码登录命令。
 */
public record OtpLoginCommand(
        @Email(message = "邮箱格式错误")
        String email,
        @Pattern(regexp = "^\\+?[0-9]{6,20}$", message = "手机号格式错误")
        String phone,
        @NotBlank(message = "验证码不能为空")
        @Length(min = 4,max = 8, message = "验证码无效")
        String codeChallenge,
        ClientContext client
) {
    @AssertTrue(message = "邮箱和手机号至少填写一项")
    public boolean hasEmailOrPhone() {
        boolean emailValid = email != null && !email.isBlank();
        boolean phoneValid = phone != null && !phone.isBlank();
        return emailValid || phoneValid;
    }
}
