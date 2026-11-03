package top.wyhao.settings.client;

/**
 * 站点配置。由 settings 基于 {@link ConfigStoreApi} 实现。
 */
public interface SiteConfigApi {

    SiteConfigVO get();

    void update(SiteConfigVO config);
}
