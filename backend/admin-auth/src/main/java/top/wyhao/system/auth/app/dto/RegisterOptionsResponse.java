package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公开注册页需要的开关和实际密码规则。不表示邮箱已经验证。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公开注册选项")
public class RegisterOptionsResponse {

    @Schema(description = "是否开放注册")
    private boolean registrationEnabled;

    @Schema(description = "密码最小长度")
    private int minLength;

    @Schema(description = "密码最大长度")
    private int maxLength;

    @Schema(description = "是否必须包含小写字母")
    private boolean requireLowercase;

    @Schema(description = "是否必须包含数字")
    private boolean requireDigit;

    @Schema(description = "是否必须包含特殊字符")
    private boolean requireSpecial;
}
