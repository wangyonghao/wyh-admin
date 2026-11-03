package top.wyhao.identity.client;

import java.util.Collection;
import java.util.List;

import top.wyhao.system.auth.domain.user.CredentialUser;

/**
 * 用户身份 API（Admin 对外提供）
 */
public interface UserApi {

    long countByDepartmentIds(Collection<Long> departmentIds);

    CredentialUser findById(Long id);

    CredentialUser findByUsername(String username);

    CredentialUser findByPhone(String phone);

    CredentialUser findByEmail(String email);

    /**
     * 第三方登录首次进入时注册用户，状态为启用。
     */
    CredentialUser registerSocialUser(String username, String displayName, Integer gender, Long departmentId);

    /**
     * 本地账号自助注册：密码策略校验 + 写入密码、过期日、角色，状态为启用。
     */
    CredentialUser registerLocalUser(String username, String rawPassword, Long departmentId, List<Long> roleIds);

    SocialLink findSocial(String source, String openId);

    void saveSocialLogin(Long userId, String source, String openId, String metaJson);

    UserProfile profile(Long id);
}
