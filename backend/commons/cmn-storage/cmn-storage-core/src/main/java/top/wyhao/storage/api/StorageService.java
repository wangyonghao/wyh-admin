package top.wyhao.storage.api;

import java.io.InputStream;

/**
 * 存储服务 SPI
 *
 * <p>各厂商实现仅依赖本接口，不得向上层暴露 SDK 类型。
 *
 * @author wyh
 * @since 2026/09/16
 */
public interface StorageService {

    /**
     * 获取存储类型
     *
     * @return 存储类型
     */
    StorageType type();

    /**
     * 上传文件
     *
     * @param key           存储键（全局唯一）
     * @param inputStream   文件输入流
     * @param contentLength 内容长度（字节数）
     * @param contentType   内容类型（MIME Type）
     */
    void put(String key, InputStream inputStream, long contentLength, String contentType);

    /**
     * 获取文件
     *
     * @param key 存储键
     * @return 存储对象；不存在时返回 {@code null}
     */
    StorageObject get(String key);

    /**
     * 删除文件（应具备幂等性）
     *
     * @param key 存储键
     */
    void delete(String key);

    /**
     * 检查文件是否存在
     *
     * @param key 存储键
     * @return 是否存在
     */
    boolean exists(String key);
}
