package top.wyhao.system.auth.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.auth.adapter.client.config.RegisterConfigVO;
import top.wyhao.settings.client.ConfigKeys;
import top.wyhao.settings.client.ConfigStoreApi;

/**
 * 注册配置：委托 settings 通用存储。
 */
@Service
@RequiredArgsConstructor
public class RegisterConfigApiImpl implements RegisterConfigApi {

    private final ConfigStoreApi configStoreApi;

    @Override
    public RegisterConfigVO get() {
        RegisterConfigVO config = configStoreApi.get(ConfigKeys.REGISTER, RegisterConfigVO.class);
        if (config == null) {
            config = new RegisterConfigVO();
        }
        return config.normalizeForRead();
    }

    @Override
    public void update(RegisterConfigVO config) {
        if (config == null) {
            config = new RegisterConfigVO();
        }
        config.normalizeForRead();
        configStoreApi.put(ConfigKeys.REGISTER, config);
    }
}
