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
     * 受理邮箱注册并发送验证链接。不登录、不签发 token。
     */
    EmailRegistrationResponse register(@Valid RegisterRequest request);

    EmailRegistrationResponse resendRegistration(@Valid EmailRegistrationResendRequest request);

    /**
     * 确认验证链接并创建账号。不签发 token。
     */
    EmailRegistrationConfirmResponse confirmRegistration(@Valid EmailRegistrationVerifyRequest request);

    RegisterOptionsResponse registerOptions();

    void logout();

    CaptchaResponse createCaptcha();

    OtpSendResponse sendOTP(@Valid OtpSendRequest req);

    OtpVerifyResponse verifyOTP(OtpVerifyRequest req);
}
