package top.wyhao.system.auth.app.service;

import jakarta.validation.Valid;
import top.wyhao.system.auth.app.dto.*;
import top.wyhao.system.auth.app.dto.GrantType;

public interface AuthService {

    /**
     * 统一登录入口。grantType 决定分支，Body 为 LoginRequest。
     */
    AccessTokenResponse login(GrantType grantType, LoginRequest req);

    /**
     * 自助注册并直接登录。
     */
    AccessTokenResponse register(@Valid RegisterRequest request);

    void logout();

    CaptchaResponse createCaptcha();

    OtpSendResponse sendOTP(@Valid OtpSendRequest req);

    OtpVerifyResponse verifyOTP(OtpVerifyRequest req);
}
