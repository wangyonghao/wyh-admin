package top.wyhao.identity.client;

/**
 * 第三方账号与用户的绑定。
 */
public record SocialLink(Long userId, String source, String openId) {
}
