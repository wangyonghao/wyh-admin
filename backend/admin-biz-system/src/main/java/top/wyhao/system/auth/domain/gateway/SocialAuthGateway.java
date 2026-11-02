package top.wyhao.system.auth.domain.gateway;

import top.wyhao.system.auth.domain.user.SocialProfile;

/**
 * 第三方 OAuth 提供方（JustAuth 等），仅负责换身份资料。
 */
public interface SocialAuthGateway {

    SocialProfile authenticate(String source, String code, String state);
}
