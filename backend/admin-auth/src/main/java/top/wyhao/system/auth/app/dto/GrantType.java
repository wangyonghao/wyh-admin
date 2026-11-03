package top.wyhao.system.auth.app.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 登录方式。
 */
@Getter
@RequiredArgsConstructor
public enum GrantType {

    /**
     * 用户名、邮箱或手机号加密码。
     */
    PASSWORD,

    /**
     * 向邮箱或手机号发送验证码后，凭验证码登录。
     */
    OTP,

    /**
     * 第三方账号
     */
    OAUTH;

    public static GrantType from(String iput) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(iput))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid GrantType: " + iput));
    }
}
