package top.wyhao.system.auth.client;


/**
 * 注册配置。由 security 基于 settings {@code ConfigStoreApi} 实现。
 */
public interface RegisterConfigApi {

    RegisterConfigVO get();

    void update(RegisterConfigVO config);
}
