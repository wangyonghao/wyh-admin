package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户自助注册请求。
 *
 * <p>只收集邮箱和原密码。用户名由服务端在当前租户内分配，仍遵守 4–64 与现有字符规则，不使用完整邮箱。
 * password 经 HTTPS 传输，服务端按密码策略校验后做慢哈希存储。提交成功不表示邮箱已经验证。
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    /**
     * 登录邮箱。按现有列长度保存，不改大小写。
     */
    @Schema(description = "邮箱。最长 64 个字符，与 sys_user.email 列一致", example = "user@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱长度不能超过 64 个字符")
    private String email;

    /**
     * 用户输入的原密码。传输安全依赖 HTTPS，服务端校验后做慢哈希存储。
     */
    @Schema(description = "原密码。经 HTTPS 传输，服务端校验后做慢哈希存储")
    @NotBlank(message = "密码不能为空")
    private String password;
}
