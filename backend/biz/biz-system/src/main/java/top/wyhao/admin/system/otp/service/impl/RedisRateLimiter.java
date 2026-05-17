package top.wyhao.admin.system.otp.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.wyhao.admin.system.otp.service.RateLimiter;

import java.util.concurrent.TimeUnit;

/**
 * Redis 限流器实现
 *
 * @author wyhao
 */
@Component
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean isAllowed(String key, int max, int window) {
        Long count = stringRedisTemplate.opsForValue().get(key) != null
            ? Long.parseLong(stringRedisTemplate.opsForValue().get(key))
            : 0L;
        return count < max;
    }

    @Override
    public long increment(String key, int window) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 0L;
        }

        // 首次设置过期时间
        if (count == 1) {
            stringRedisTemplate.expire(key, window, TimeUnit.SECONDS);
        }

        return count;
    }

    @Override
    public long getCount(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
