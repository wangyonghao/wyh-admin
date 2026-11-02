package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应参数
 *
 * @since 2022/12/21 20:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应参数")
public class AccessTokenResponse {
    /**
     * 响应代码
     */
    @Schema(description = "响应代码", example = "200")
    private String code;

    /**
     * 令牌
     */
    @Schema(description = "令牌", example = "ey****J9.ey****fQ.KU****Z8")
    private String token;
}
