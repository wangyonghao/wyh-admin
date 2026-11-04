package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.SocialIdentity;

/**
 * 个人中心绑定用的第三方 OAuth 换身份。
 */
public interface SocialOAuthGateway {

    SocialIdentity authenticate(String source, String code, String state);
}
