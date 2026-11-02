package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.settings.client.ConfigStoreApi;
import top.wyhao.system.admin.app.service.ConfigStoreAppService;

/**
 * 通用配置存储 API 适配：只委托 app 编排。
 */
@Service
@RequiredArgsConstructor
public class ConfigStoreApiImpl implements ConfigStoreApi {

    private final ConfigStoreAppService configStoreAppService;

    @Override
    public <T> T get(String configKey, Class<T> type) {
        return configStoreAppService.get(configKey, type);
    }

    @Override
    public void put(String configKey, Object config) {
        configStoreAppService.put(configKey, config);
    }
}
