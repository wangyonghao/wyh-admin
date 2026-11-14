package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.identity.client.EmailRegistrationApi;
import top.wyhao.identity.client.EmailRegistrationConfirm;
import top.wyhao.identity.client.EmailRegistrationStart;
import top.wyhao.identity.client.EmailRegistrationTicket;
import top.wyhao.system.admin.app.service.impl.EmailRegistrationService;

/**
 * 邮箱注册验证的模块边界。实现留在 admin-system，auth 只依赖这条契约。
 */
@Service
@RequiredArgsConstructor
public class EmailRegistrationApiImpl implements EmailRegistrationApi {

    private final EmailRegistrationService emailRegistrationService;

    @Override
    public EmailRegistrationTicket begin(EmailRegistrationStart start) {
        return emailRegistrationService.begin(start);
    }

    @Override
    public EmailRegistrationTicket resend(String email, String publicBaseUrl, int ttlMinutes, int resendIntervalSeconds) {
        return emailRegistrationService.resend(email, publicBaseUrl, ttlMinutes, resendIntervalSeconds);
    }

    @Override
    public EmailRegistrationConfirm confirm(String token, boolean registrationEnabled) {
        return emailRegistrationService.confirm(token, registrationEnabled);
    }
}
