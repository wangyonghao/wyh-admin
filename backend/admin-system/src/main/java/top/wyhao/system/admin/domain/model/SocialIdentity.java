package top.wyhao.system.admin.domain.model;

/**
 * 第三方身份资料（自有模型，不依赖 JustAuth）。
 */
public record SocialIdentity(
        String source,
        String openId,
        String username,
        String metaJson
) {
}
