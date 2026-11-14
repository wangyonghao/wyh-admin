package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 确认注册链接。token 放在请求体里，避免出现在查询串和 Referer。
 */
@Data
@Schema(description = "确认邮箱注册")
public class EmailRegistrationVerifyRequest {

    @Schema(description = "邮件链接中的一次性 token")
    @NotBlank(message = "链接无效或已过期")
    @Size(max = 128, message = "链接无效或已过期")
    private String token;
}
