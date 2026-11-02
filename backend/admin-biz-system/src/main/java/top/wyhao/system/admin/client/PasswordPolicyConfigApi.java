package top.wyhao.system.admin.client;

/**
 * 密码策略配置。由 identity 基于 settings {@code ConfigStoreApi} 实现。
 */
public interface PasswordPolicyConfigApi {

    PasswordPolicyConfig get();

    void update(PasswordPolicyConfig config);
}
