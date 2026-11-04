package top.wyhao.system.auth.client;


/**
 * 登录 / 认证配置。由 security 基于 settings {@code ConfigStoreApi} 实现。
 */
public interface LoginConfigApi {

    LoginConfigVO get();

    void update(LoginConfigVO config);
}
