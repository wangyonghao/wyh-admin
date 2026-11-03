package top.wyhao.system.auth.domain.otp;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;

/**
 * 一次性验证码。code 只生成一次，存 Redis 与投递必须用同一份。
 *
 * @param identifier   邮箱或手机号
 * @param uuid         会话 ID，供 verify 绑定
 * @param code         验证码明文
 * @param expireSeconds 有效期（秒），与配置一致
 * @param expireAt     过期时间点
 */
public record Otp(
        String identifier,
        String uuid,
        String code,
        int expireSeconds,
        LocalDateTime expireAt
) {

    public static Otp generate(String identifier, int codeLength, int expireSeconds, LocalDateTime now) {
        String code = OtpGenerator.generate(codeLength);
        String uuid = IdUtil.fastSimpleUUID();
        return new Otp(identifier, uuid, code, expireSeconds, now.plusSeconds(expireSeconds));
    }

    /** 文案展示用分钟数（至少 1）。 */
    public int expireMinutes() {
        return Math.max(1, expireSeconds / 60);
    }

    public boolean matches(String input) {
        return StrUtil.isNotBlank(input) && code.equals(input);
    }

    public boolean isExpired(LocalDateTime now) {
        return now == null || !now.isBefore(expireAt);
    }
}
