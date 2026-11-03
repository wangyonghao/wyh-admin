package top.wyhao.system.admin.infrastructure.storage;

import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

/**
 * 存储管理器
 *
 * <p>根据存储类型路由到 classpath 上已注册的 {@link StorageService} 实现。
 *
 * @author wyh
 * @since 2026/09/16
 */
public interface StorageManager {

    /**
     * 根据存储类型获取对应的存储服务
     *
     * @param type 存储类型
     * @return 存储服务
     */
    StorageService get(StorageType type);

    /**
     * 获取默认的存储服务
     *
     * @return 默认存储服务
     */
    StorageService getDefault();
}
