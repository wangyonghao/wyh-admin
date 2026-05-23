
package top.wyhao.admin.auth.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.wyhao.admin.auth.model.enums.AuthType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求参数基类
 *


 * @since 2024/12/22 15:16
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "authType", visible = true)
@JsonSubTypes({@JsonSubTypes.Type(value = AccountLoginRequest.class, name = "ACCOUNT"),
    @JsonSubTypes.Type(value = EmailLoginRequest.class, name = "EMAIL"),
    @JsonSubTypes.Type(value = PhoneLoginRequest.class, name = "PHONE"),
    @JsonSubTypes.Type(value = SocialLoginRequest.class, name = "SOCIAL")})
@Schema(description = "登录请求参数基类")
public class LoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端 ID
     */
    @Schema(description = "客户端 ID", example = "ef51c9a3e9046c4f2ea45142c8a8344a")
    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    /**
     * 认证类型
     */
    @Schema(description = "认证类型", example = "ACCOUNT")
    @NotNull(message = "认证类型无效")
    private AuthType authType;
}
