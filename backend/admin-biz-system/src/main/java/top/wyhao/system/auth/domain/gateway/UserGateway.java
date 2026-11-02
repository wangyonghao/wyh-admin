package top.wyhao.system.auth.domain.gateway;

import top.wyhao.system.auth.domain.user.CredentialUser;
import top.wyhao.system.admin.client.SocialLink;
import top.wyhao.system.auth.domain.user.SocialProfile;

/**
 * 认证所需的身份域能力：凭证查询、部门/租户、社交供给。
 */
public interface UserGateway {

    CredentialUser findByUsername(String username);

    CredentialUser findByEmail(String email);

    CredentialUser findByPhone(String phone);

    CredentialUser findById(Long id);

    SocialLink findSocial(String source, String openId);

    boolean isDepartmentDisabled(Long departmentId);

    Long resolveLoginTenantId(Long userId);

    CredentialUser registerSocialUser(String username, String displayName, Integer gender);

    void assignGeneralUserRole(Long userId);

    void notifyWelcome(CredentialUser user);

    /**
     * 密码已过期或即将过期时发送站内提醒（不阻断登录）。
     */
    void notifyPasswordExpiryReminder(CredentialUser user);

    void saveSocialLogin(Long userId, SocialProfile profile);
}
