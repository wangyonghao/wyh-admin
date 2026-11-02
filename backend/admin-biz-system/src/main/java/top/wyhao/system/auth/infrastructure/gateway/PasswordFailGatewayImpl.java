package top.wyhao.system.auth.infrastructure.gateway;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.springframework.stereotype.Component;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.system.auth.adapter.client.LoginConfigApi;
import top.wyhao.system.auth.adapter.client.config.LoginConfigVO;
import top.wyhao.system.auth.domain.gateway.PasswordFailGateway;
import top.wyhao.system.auth.domain.user.PasswordPolicy;

import java.time.Duration;

/**
 * 登录失败记录：账号 / IP 双维度计数与锁定。
 */
@Component
@RequiredArgsConstructor
public class PasswordFailGatewayImpl implements PasswordFailGateway {

    private final LoginConfigApi loginConfigApi;

    private String getFailKey(LimitDimension dimension, String key) {
        return "login:fail:" + dimension + ":" + key;
    }

    private String getLockKey(LimitDimension dimension, String key) {
        return "login:lock:" + dimension + ":" + key;
    }

    @Override
    public int getFailCount(String account) {
        RAtomicLong counter = RedisUtils.getClient().getAtomicLong(getFailKey(LimitDimension.ACCOUNT, account));
        return Math.toIntExact(counter.get());
    }

    @Override
    public boolean isBanned(LimitDimension dimension, String key) {
        return RedisUtils.exists(getLockKey(dimension, key));
    }

    @Override
    public int getRemainMinutes(LimitDimension dimension, String key) {
        long ttlMs = RedisUtils.getTimeToLive(getLockKey(dimension, key));
        if (ttlMs <= 0) {
            return 0;
        }
        return (int) Math.max(1L, (ttlMs + 59_999L) / 60_000L);
    }

    @Override
    public int increment(String account, String ip, int durationMinutes) {
        PasswordPolicy policy = currentPolicy();
        Duration ttl = Duration.ofMinutes(durationMinutes);

        RAtomicLong accountCounter = RedisUtils.getClient().getAtomicLong(getFailKey(LimitDimension.ACCOUNT, account));
        int accountCount = Math.toIntExact(accountCounter.incrementAndGet());
        accountCounter.expire(ttl);

        RAtomicLong ipCounter = RedisUtils.getClient().getAtomicLong(getFailKey(LimitDimension.IP, ip));
        int ipCount = Math.toIntExact(ipCounter.incrementAndGet());
        ipCounter.expire(ttl);

        if (accountCount >= policy.maxRetry()) {
            RedisUtils.set(getLockKey(LimitDimension.ACCOUNT, account), "1", ttl);
        }
        if (ipCount >= policy.maxRetry()) {
            RedisUtils.set(getLockKey(LimitDimension.IP, ip), "1", ttl);
        }
        return accountCount;
    }

    @Override
    public void clear(String key) {
        RedisUtils.getClient().getAtomicLong(getFailKey(LimitDimension.ACCOUNT, key)).delete();
        RedisUtils.delete(getLockKey(LimitDimension.ACCOUNT, key));
    }

    @Override
    public PasswordPolicy currentPolicy() {
        LoginConfigVO config = loginConfigApi.get();
        if (config == null) {
            return PasswordPolicy.of(null, null, null);
        }
        return PasswordPolicy.of(config.getMaxRetry(), config.getLockTime(), config.resolvedCaptchaErrorThreshold());
    }
}
