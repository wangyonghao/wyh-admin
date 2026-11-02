package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import top.wyhao.cmn.core.constant.RegexConstants;

/**
 * 用户自助注册请求。
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = RegexConstants.USERNAME, message = "用户名长度为 4-64 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String username;

    @Schema(description = "密码（RSA 公钥加密）")
    @NotBlank(message = "密码不能为空")
    private String password;
}
