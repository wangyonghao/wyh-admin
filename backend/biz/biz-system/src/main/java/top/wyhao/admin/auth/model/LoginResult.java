
package top.wyhao.admin.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应参数
 *

 * @since 2022/12/21 20:42
 */
@Data
@Builder
@Schema(description = "登录响应参数")
public class LoginResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应代码
     */
    @Schema(description = "响应代码", example = "PASSWORD_EXPIRED")
    private String code;

    /**
     * 令牌
     */
    @Schema(description = "令牌", example = "ey****J9.ey****fQ.KU****Z8")
    private String token;

    /**
     * 租户 ID
     */
    @Schema(description = "租户 ID", example = "0")
    private Long tenantId;
}
