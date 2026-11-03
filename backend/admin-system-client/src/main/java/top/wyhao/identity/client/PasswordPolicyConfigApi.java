package top.wyhao.identity.client;

/**
 * 密码策略配置。由 Admin 基于 settings {@code ConfigStoreApi} 实现。
 */
public interface PasswordPolicyConfigApi {

    PasswordPolicyConfig get();

    void update(PasswordPolicyConfig config);
}
