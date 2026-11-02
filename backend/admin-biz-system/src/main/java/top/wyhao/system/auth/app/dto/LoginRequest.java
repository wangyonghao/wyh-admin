package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 统一登录请求体。具体必填字段由 Query 参数 grantType 决定，不在此类上做互斥校验。
 *
 * 密码登录(grantType=PASSWORD)时，username,email,phone 三选一，password 必须；仅当验证码启用时且达到阈值时提供 captcha_uuid，captcha_challenge
 * 一次性密码登录(grantType=OTP)时, email/phone 二选一，email有值时邮件发送验证码，phone有值时短信发送验证码
 */
@Data
@Schema(description = "登录请求参数")
public class LoginRequest {

    @NotNull
    @Schema(description = "客户端 ID", example = "ef51c9a3e9046c4f2ea45142c8a8344a")
    private String clientId;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "zhangsan")
    private String username;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "zhangsan@mail.com")
    private String email;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "+8613600120012")
    private String phone;

    @Schema(description = "密码（密码登录）", example = "admin123")
    private String password;

    @Schema(description = "验证码标识，仅当登录验证码启用时提供", example = "090b9a2c-1691-4fca-99db-e4ed0cff362f")
    private String captchaUuid;

    @Schema(description = "验证码，仅当登录验证码启用时提供", example = "ABCD")
    private String captchaChallenge;

    @Schema(description = "OTP验证码", example = "ABCD")
    private String codeChallenge;

    @Schema(description = "第三方身份平台", example = "gitee")
    private String provider;

    @Schema(description = "授权码（第三方登录）", example = "a08d33e9e577fb339de027499784ed4e871d6f62ae65b459153e906ab546bd56")
    private String code;

    @Schema(description = "状态码（第三方登录）", example = "2ca8d8baf437eb374efaa1191a3d")
    private String state;
}
