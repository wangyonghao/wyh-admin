package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import top.wyhao.cmn.core.constant.RegexConstants;

/**
 * 用户自助注册请求。
 *
 * <p>password 为用户输入的原密码，传输安全依赖 HTTPS。服务端按密码策略校验后做慢哈希存储。
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    @Schema(description = "用户名，4-64 个字符，以字母开头，支持大小写字母、数字、下划线", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = RegexConstants.USERNAME, message = "用户名长度为 4-64 个字符，支持大小写字母、数字、下划线，以字母开头")
    private String username;

    /**
     * 用户输入的原密码。传输安全依赖 HTTPS，服务端校验后做慢哈希存储。
     */
    @Schema(description = "原密码。经 HTTPS 传输，服务端校验后做慢哈希存储")
    @NotBlank(message = "密码不能为空")
    private String password;
}
