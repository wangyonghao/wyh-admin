package top.wyhao.system.admin.client;

import java.util.Collection;

import top.wyhao.identity.client.UserProfile;
import top.wyhao.system.auth.domain.user.CredentialUser;

/**
 * 用户身份 API（身份域对外提供）
 */
public interface UserApi {

    /**
     * 统计部门下的用户数
     */
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
     * 本地账号自助注册：写入密码、过期日、角色，状态为启用。
     *
     * @param username    用户名
     * @param rawPassword 明文密码（已解密、已校验策略）
     * @param departmentId      所属部门
     * @param roleIds     初始角色
     */
    CredentialUser registerLocalUser(String username, String rawPassword, Long departmentId, java.util.List<Long> roleIds);

    SocialLink findSocial(String source, String openId);

    void saveSocialLogin(Long userId, String source, String openId, String metaJson);

    UserProfile profile(Long id);
}
