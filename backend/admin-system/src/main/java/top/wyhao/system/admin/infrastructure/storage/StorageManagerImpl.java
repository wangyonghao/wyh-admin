package top.wyhao.system.admin.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wyhao.system.admin.infrastructure.config.FileCoreProperties;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储管理器实现
 *
 * <p>通过构造注入自动收集 classpath 上所有 {@link StorageService} Bean，
 * 存储实现模块无需感知本类。
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
@Component
public class StorageManagerImpl implements StorageManager {

    private final Map<StorageType, StorageService> storageServices = new ConcurrentHashMap<>();
    private final FileCoreProperties properties;

    public StorageManagerImpl(List<StorageService> storageServices, FileCoreProperties properties) {
        this.properties = properties;
        List<StorageService> services = storageServices != null ? storageServices : Collections.emptyList();
        for (StorageService storageService : services) {
            this.storageServices.put(storageService.type(), storageService);
            log.info("注册存储服务: type={}, class={}",
                    storageService.type(), storageService.getClass().getSimpleName());
        }
    }

    @Override
    public StorageService get(StorageType type) {
        StorageService service = storageServices.get(type);
        if (service == null) {
            throw new IllegalArgumentException("未找到存储服务: " + type);
        }
        return service;
    }

    @Override
    public StorageService getDefault() {
        return get(properties.getStorage().getType());
    }
}
