package top.wyhao.settings.client;

/**
 * 通用配置存储。业务域通过本 API 按 key 读写 JSON 配置，不必修改 settings 模块代码。
 */
public interface ConfigStoreApi {

    /**
     * 按键读取并反序列化为指定类型；不存在或值为空时返回 {@code null}。
     */
    <T> T get(String configKey, Class<T> type);

    /**
     * 按键写入（存在则更新，不存在则创建）。
     */
    void put(String configKey, Object config);
}
