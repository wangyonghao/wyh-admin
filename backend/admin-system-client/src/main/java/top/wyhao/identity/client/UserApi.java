package top.wyhao.identity.client;

import java.util.Collection;
import java.util.List;

/**
 * 用户身份 API（Admin 对外提供）
 */
public interface UserApi {

    long countByDepartmentIds(Collection<Long> departmentIds);

    UserCredential findById(Long id);

    UserCredential findByUsername(String username);

    UserCredential findByPhone(String phone);

    UserCredential findByEmail(String email);

    /**
     * 第三方登录首次进入时注册用户，状态为启用。
     */
    UserCredential registerSocialUser(String username, String displayName, Integer gender, Long departmentId);

    /**
     * 本地账号自助注册：密码策略校验 + 写入密码、过期日、角色，状态为启用。
     */
    UserCredential registerLocalUser(String username, String rawPassword, Long departmentId, List<Long> roleIds);

    /**
     * 邮箱自助注册。服务端分配用户名，写入邮箱和慢哈希密码。不表示邮箱已验证。
     */
    UserCredential registerWithEmail(String email, String rawPassword, Long departmentId, List<Long> roleIds);

    SocialLink findSocial(String source, String openId);

    void saveSocialLogin(Long userId, String source, String openId, String metaJson);

    UserProfile profile(Long id);
}
