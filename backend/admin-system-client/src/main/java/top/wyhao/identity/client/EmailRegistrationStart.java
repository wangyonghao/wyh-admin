package top.wyhao.identity.client;

/**
 * 开始一次邮箱注册。原密码只在这次调用里出现，落地前做慢哈希。
 */
public record EmailRegistrationStart(
        String email,
        String rawPassword,
        Long departmentId,
        Long roleId,
        String publicBaseUrl,
        int ttlMinutes,
        int resendIntervalSeconds
) {
}
