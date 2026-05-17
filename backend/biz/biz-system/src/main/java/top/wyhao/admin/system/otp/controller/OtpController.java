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

/**
 * OTP 验证码 API
 * 解决“凭证安全/防冒充”问题
 *
 *
 * @author wyhao
 */
@Tag(name = "OTP 验证码")
@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @Operation(summary = "发送验证码", description = "发送邮件或短信验证码")
    @PostMapping("/send")
    public OtpSendResult send(@Valid @RequestBody OtpSendRequest req) {
        return otpService.send(req);
    }

    @Operation(summary = "验证验证码", description = "验证用户输入的验证码")
    @PostMapping("/verify")
    public OtpVerifyResult verify(@Valid @RequestBody OtpVerifyRequest req) {
        return otpService.verify(req);
    }
}
