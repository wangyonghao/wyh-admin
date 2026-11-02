package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.cmn.sms.SmsConfig;
import top.wyhao.settings.client.ConfigKeys;
import top.wyhao.settings.client.ConfigStoreApi;
import top.wyhao.settings.client.SmsConfigApi;

/**
 * 短信配置：委托 settings 通用存储。
 */
@Service
@RequiredArgsConstructor
public class SmsConfigApiImpl implements SmsConfigApi {

    private final ConfigStoreApi configStoreApi;

    @Override
    public SmsConfig getSmsConfig() {
        return configStoreApi.get(ConfigKeys.SMS, SmsConfig.class);
    }

    @Override
    public void updateSmsConfig(Object config) {
        configStoreApi.put(ConfigKeys.SMS, config);
    }

    @Override
    public String getSmsTemplate(String scene) {
        return configStoreApi.get(ConfigKeys.smsTemplate(scene), String.class);
    }
}
