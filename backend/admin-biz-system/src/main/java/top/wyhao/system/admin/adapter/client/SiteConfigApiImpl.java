package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.settings.client.ConfigKeys;
import top.wyhao.settings.client.ConfigStoreApi;
import top.wyhao.settings.client.SiteConfigApi;
import top.wyhao.settings.client.SiteConfigVO;

/**
 * 站点配置：委托通用存储。
 */
@Service
@RequiredArgsConstructor
public class SiteConfigApiImpl implements SiteConfigApi {

    private final ConfigStoreApi configStoreApi;

    @Override
    public SiteConfigVO get() {
        return configStoreApi.get(ConfigKeys.SITE, SiteConfigVO.class);
    }

    @Override
    public void update(SiteConfigVO config) {
        configStoreApi.put(ConfigKeys.SITE, config);
    }
}
