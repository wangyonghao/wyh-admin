package top.wyhao.admin.system.otp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.admin.system.otp.model.request.OtpSendRequest;
import top.wyhao.admin.system.otp.model.request.OtpVerifyRequest;
import top.wyhao.admin.system.otp.model.result.OtpSendResult;
import top.wyhao.admin.system.otp.model.result.OtpVerifyResult;
import top.wyhao.admin.system.otp.service.OtpService;
import top.wyhao.starter.web.ratelimit.LimitType;
import top.wyhao.starter.web.ratelimit.RateLimiter;
import top.wyhao.starter.web.ratelimit.RateLimiters;

import java.util.concurrent.TimeUnit;

/**
 * OTP(One-Time Password) 验证码 API
 * 使用场景：用户注册/安全登录/找回密码/手机绑定/身份验证/支付认证
 * 解决“凭证安全/防冒充”问题
 *
 *

 */
@Tag(name = "OTP 验证码")
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class OtpController {
    private static final String OTP_KEY_PREFIX = "otp:captcha:";

    private final OtpService otpService;

    @Operation(summary = "发送验证码", description = "发送邮件或短信验证码")
    @RateLimiters({
            @RateLimiter(name = OTP_KEY_PREFIX + "MIN", key = "#req + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.sms.templateId')", rate = 2, interval = 1, unit = TimeUnit.MINUTES, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = OTP_KEY_PREFIX + "HOUR", key = "#phone + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.sms.templateId')", rate = 8, interval = 1, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = OTP_KEY_PREFIX + "DAY'", key = "#phone + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.sms.templateId')", rate = 20, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = OTP_KEY_PREFIX, key = "#phone", rate = 100, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = OTP_KEY_PREFIX, key = "#phone", rate = 30, interval = 1, unit = TimeUnit.MINUTES, type = LimitType.IP, message = "获取验证码操作太频繁，请稍后再试")})

    @PostMapping("/v1/otp/mail/send")
    public OtpSendResult sendByMail(@Valid @RequestBody OtpSendRequest req) {
        return otpService.sendByMail(req);
    }

    @PostMapping("/v1/otp/sms/send")
    public OtpSendResult sendBySms(@Valid @RequestBody OtpSendRequest req) {
        return otpService.sendBySms(req);
    }


    @Operation(summary = "验证验证码", description = "验证用户输入的验证码")
    @PostMapping("/v1/otp/verify")
    public OtpVerifyResult verify(@Valid @RequestBody OtpVerifyRequest req) {
        return otpService.verify(req);
    }
}
