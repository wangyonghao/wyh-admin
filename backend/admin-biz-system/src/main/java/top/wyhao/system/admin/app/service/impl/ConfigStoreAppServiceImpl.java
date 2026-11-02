package top.wyhao.system.admin.app.service.impl;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.admin.app.service.ConfigStoreAppService;
import top.wyhao.system.admin.domain.exception.ConfigException;
import top.wyhao.system.admin.domain.gateway.ConfigRepository;
import top.wyhao.system.admin.domain.model.Config;

/**
 * 通用配置键值存储实现。
 */
@Service
@RequiredArgsConstructor
public class ConfigStoreAppServiceImpl implements ConfigStoreAppService {

    private final ConfigRepository configRepository;

    @Override
    public <T> T get(String configKey, Class<T> type) {
        return configRepository.getAs(configKey, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void put(String configKey, Object config) {
        Config existConfig = configRepository.findByKey(configKey);
        String configValue = JSONUtil.toJsonStr(config);

        if (existConfig != null) {
            Config updateConfig = new Config();
            updateConfig.setId(existConfig.getId());
            updateConfig.setConfigValue(configValue);
            int updated = configRepository.updateById(updateConfig);
            if (updated <= 0) {
                throw ConfigException.updateFailed();
            }
            return;
        }

        Config newConfig = new Config();
        newConfig.setConfigKey(configKey);
        newConfig.setConfigValue(configValue);
        newConfig.setDescription(configKey + "配置");
        configRepository.insert(newConfig);
    }
}
