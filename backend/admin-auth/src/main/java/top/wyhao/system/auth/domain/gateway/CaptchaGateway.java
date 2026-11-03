package top.wyhao.system.auth.domain.gateway;

import top.wyhao.system.auth.domain.model.ImageCaptcha;

/**
 * 图形验证码底层能力：生成图片并缓存、一次性校验。
 */
public interface CaptchaGateway {

    /**
     * 生成图形验证码并写入缓存，返回 uuid / 图片 / 过期时间。
     * {@code alwaysRequired} 由上层按登录策略填充，此处固定为 false。
     */
    ImageCaptcha generate();

    /**
     * 一次性校验图形验证码：消费缓存并比对。
     * uuid/答案为空、已失效或不匹配时抛出 {@link top.wyhao.system.auth.domain.exception.AuthenticationException}。
     */
    void verify(String uuid, String captcha);
}
