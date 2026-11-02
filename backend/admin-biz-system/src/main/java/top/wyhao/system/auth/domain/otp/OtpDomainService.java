package top.wyhao.system.auth.domain.otp;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.system.auth.domain.gateway.OtpDeliveryGateway;
import top.wyhao.system.auth.domain.gateway.OtpGateway;

import java.time.LocalDateTime;

/**
 * OTP 领域服务：只生成一次 code，存发同一份；校验比对后一次性消费。
 * 不依赖 app DTO / Servlet / 具体 MailSms 实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpDomainService {

    private final OtpGateway otpGateway;
    private final OtpDeliveryGateway otpDeliveryGateway;

    /**
     * 生成并缓存 OTP（单一 code）。
     */
    public Otp issue(String identifier) {
        if (StrUtil.isBlank(identifier)) {
            throw OtpException.invalidTarget("目标地址不能为空");
        }
        int length = otpGateway.codeLength();
        int expireSeconds = otpGateway.expireInSeconds();
        Otp otp = Otp.generate(identifier, length, expireSeconds, LocalDateTime.now());
        otpGateway.save(identifier, otp.code(), expireSeconds);
        otpGateway.saveSession(otp.uuid(), identifier, expireSeconds);
        return otp;
    }

    public void sendEmail(String email, OtpScene scene, String locale, Otp otp, String clientIp) {
        otpDeliveryGateway.sendEmail(
                email, scene, locale, otp.code(), otp.expireMinutes(),
                CharSequenceUtil.blankToDefault(clientIp, "unknown"));
        log.info("OTP 发送成功: channel=EMAIL, scene={}, target={}", scene, email);
    }

    public void sendSms(String phone, OtpScene scene, Otp otp) {
        otpDeliveryGateway.sendSms(phone, scene, otp.code(), otp.expireMinutes());
        log.info("OTP 发送成功: channel=SMS, scene={}, target={}", scene, phone);
    }

    /**
     * 校验验证码：比对 code；若提供 otpUuid 则校验会话绑定；成功后一次性删除。
     */
    public void verify(String contact, String code, String otpUuid) {
        if (StrUtil.isBlank(contact)) {
            throw OtpException.invalidTarget("目标地址不能为空");
        }
        if (StrUtil.isBlank(code)) {
            throw OtpException.invalid();
        }

        String cached = otpGateway.get(contact);
        if (CharSequenceUtil.isBlank(cached)) {
            throw OtpException.expired();
        }
        if (!cached.equals(code)) {
            throw OtpException.invalid();
        }

        if (StrUtil.isNotBlank(otpUuid)) {
            String bound = otpGateway.getContactBySession(otpUuid);
            if (CharSequenceUtil.isBlank(bound) || !bound.equals(contact)) {
                throw OtpException.notFound();
            }
        }

        otpGateway.delete(contact);
        if (StrUtil.isNotBlank(otpUuid)) {
            otpGateway.deleteSession(otpUuid);
        }
        log.info("OTP 验证成功: contact={}, uuid={}", contact, otpUuid);
    }
}
