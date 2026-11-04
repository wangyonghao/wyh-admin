package top.wyhao.system.auth.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.auth.client.LoginConfigApi;
import top.wyhao.system.auth.client.LoginConfigVO;
import top.wyhao.settings.client.ConfigKeys;
import top.wyhao.settings.client.ConfigStoreApi;

/**
 * 登录配置：委托 settings 通用存储。
 */
@Service
@RequiredArgsConstructor
public class LoginConfigApiImpl implements LoginConfigApi {

    private final ConfigStoreApi configStoreApi;

    @Override
    public LoginConfigVO get() {
        LoginConfigVO config = configStoreApi.get(ConfigKeys.LOGIN, LoginConfigVO.class);
        if (config == null) {
            config = new LoginConfigVO();
        }
        return config.normalizeForRead();
    }

    @Override
    public void update(LoginConfigVO config) {
        if (config == null) {
            config = new LoginConfigVO();
        }
        config.normalizeAndValidateForWrite();
        configStoreApi.put(ConfigKeys.LOGIN, config);
    }
}
