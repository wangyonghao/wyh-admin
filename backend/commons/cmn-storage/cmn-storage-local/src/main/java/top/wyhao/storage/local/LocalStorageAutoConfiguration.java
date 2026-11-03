package top.wyhao.storage.local;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

/**
 * 本地存储自动配置
 *
 * <p>激活条件：{@code file.storage.type=LOCAL}（未配置时默认激活）
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(LocalStorageProperties.class)
@ConditionalOnProperty(name = "file.storage.type", havingValue = "LOCAL", matchIfMissing = true)
public class LocalStorageAutoConfiguration {

    private final LocalStorageProperties properties;

    @Bean
    public StorageService localStorageService() {
        LocalStorageService storage = new LocalStorageService(properties);
        log.info("已注册本地存储服务: storageType={}, rootPath={}",
                StorageType.LOCAL, properties.getRootPath());
        return storage;
    }
}
