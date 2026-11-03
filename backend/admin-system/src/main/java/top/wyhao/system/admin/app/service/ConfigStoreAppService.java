package top.wyhao.system.admin.app.service;

/**
 * 通用配置键值存储编排（事务边界在此，不在 adapter/client）。
 */
public interface ConfigStoreAppService {

    <T> T get(String configKey, Class<T> type);

    void put(String configKey, Object config);
}
