package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 三方 OAuth 回调参数（HTTP 契约，不暴露 JustAuth 类型）。
 */
@Data
@Schema(description = "三方 OAuth 回调")
public class SocialOAuthCallbackRequest {

    @Schema(description = "授权码")
    private String code;

    @Schema(description = "state")
    private String state;
}
