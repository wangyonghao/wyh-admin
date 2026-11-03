package top.wyhao.system.auth.infrastructure.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.system.auth.domain.gateway.OtpGateway;
import top.wyhao.system.auth.infrastructure.config.OtpProperties;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OtpGatewayImpl implements OtpGateway {

    private static final String OTP_KEY = "login:otp:";
    private static final String OTP_SESSION_KEY = "login:otp:session:";

    private final OtpProperties otpProperties;

    @Override
    public int expireInSeconds() {
        Integer expiresIn = otpProperties.getCode().getExpiresIn();
        return expiresIn != null && expiresIn > 0 ? expiresIn : 300;
    }

    @Override
    public int codeLength() {
        Integer length = otpProperties.getCode().getLength();
        return length != null && length > 0 ? length : 6;
    }

    @Override
    public void save(String contact, String code, long ttlSeconds) {
        RedisUtils.set(OTP_KEY + contact, code, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void saveSession(String otpUuid, String contact, long ttlSeconds) {
        RedisUtils.set(OTP_SESSION_KEY + otpUuid, contact, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public String get(String contact) {
        return RedisUtils.get(OTP_KEY + contact);
    }

    @Override
    public String getContactBySession(String otpUuid) {
        return RedisUtils.get(OTP_SESSION_KEY + otpUuid);
    }

    @Override
    public void delete(String contact) {
        RedisUtils.delete(OTP_KEY + contact);
    }

    @Override
    public void deleteSession(String otpUuid) {
        RedisUtils.delete(OTP_SESSION_KEY + otpUuid);
    }
}
