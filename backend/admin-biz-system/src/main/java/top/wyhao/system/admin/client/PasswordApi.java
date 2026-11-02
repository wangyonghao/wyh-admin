package top.wyhao.system.admin.client;

/**
 * 用户密码凭证管理（由 identity 实现）。
 */
public interface PasswordApi {

    void resetPassword(Long userId, String rawPassword);

    String resetToRandomPassword(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void assertMatches(Long userId, String rawPassword);
}
