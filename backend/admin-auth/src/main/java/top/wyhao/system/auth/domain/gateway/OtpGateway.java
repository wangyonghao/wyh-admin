package top.wyhao.system.auth.domain.gateway;

/**
 * 联系方式 OTP 缓存（读写删除）及码长 / TTL 配置。
 */
public interface OtpGateway {

    /** 验证码有效期（秒），与配置 otp.code.expires-in 一致。 */
    int expireInSeconds();

    /** 验证码位数。 */
    int codeLength();

    /**
     * 按联系方式保存验证码，TTL 为秒。
     */
    void save(String contact, String code, long ttlSeconds);

    /**
     * 绑定会话 uuid → 联系方式，TTL 与验证码一致，供 verify 校验 otpUuid。
     */
    void saveSession(String otpUuid, String contact, long ttlSeconds);

    String get(String contact);

    String getContactBySession(String otpUuid);

    void delete(String contact);

    void deleteSession(String otpUuid);
}
