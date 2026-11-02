package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.client.PasswordPolicyConfig;
import top.wyhao.system.admin.client.PasswordPolicyConfigApi;
import top.wyhao.settings.client.ConfigKeys;
import top.wyhao.settings.client.ConfigStoreApi;

/**
 * 密码策略配置：委托 settings 通用存储。
 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyConfigApiImpl implements PasswordPolicyConfigApi {

    private final ConfigStoreApi configStoreApi;

    @Override
    public PasswordPolicyConfig get() {
        return configStoreApi.get(ConfigKeys.PASSWORD_POLICY, PasswordPolicyConfig.class);
    }

    @Override
    public void update(PasswordPolicyConfig config) {
        configStoreApi.put(ConfigKeys.PASSWORD_POLICY, config);
    }
}
