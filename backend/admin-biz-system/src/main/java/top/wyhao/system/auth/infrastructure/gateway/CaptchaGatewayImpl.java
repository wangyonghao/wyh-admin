package top.wyhao.system.auth.infrastructure.gateway;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.common.graphic.core.ImageCaptchaService;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.system.auth.domain.exception.AuthenticationException;
import top.wyhao.system.auth.domain.gateway.CaptchaGateway;
import top.wyhao.system.auth.domain.model.ImageCaptcha;
import top.wyhao.system.auth.infrastructure.config.CaptchaProperties;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 图形验证码：EasyCaptcha 出图 + Redis 一次性缓存。
 */
@Component
@RequiredArgsConstructor
public class CaptchaGatewayImpl implements CaptchaGateway {

    private static final String IMAGE_CAPTCHA_KEY = "login:captcha:";

    private final CaptchaProperties captchaProperties;
    private final ImageCaptchaService imageCaptchaService;

    @Override
    public ImageCaptcha generate() {
        Captcha captcha = imageCaptchaService.createCaptchaImage();
        long expireTime = LocalDateTimeUtil.toEpochMilli(
                LocalDateTime.now().plusSeconds(captchaProperties.getExpirationInSeconds()));
        String uuid = IdUtil.fastUUID();
        RedisUtils.set(
                IMAGE_CAPTCHA_KEY + uuid,
                captcha.text(),
                Duration.ofSeconds(captchaProperties.getExpirationInSeconds()));
        return new ImageCaptcha(uuid, captcha.toBase64(), expireTime, false);
    }

    @Override
    public void verify(String uuid, String captcha) {
        if (StrUtil.isBlank(uuid) || StrUtil.isBlank(captcha)) {
            throw AuthenticationException.captchaRequired();
        }
        String cached = RedisUtils.getAndDelete(IMAGE_CAPTCHA_KEY + uuid);
        if (StrUtil.isBlank(cached)) {
            throw AuthenticationException.captchaExpired();
        }
        if (!cached.equalsIgnoreCase(captcha.trim())) {
            throw AuthenticationException.captchaIncorrect();
        }
    }
}
