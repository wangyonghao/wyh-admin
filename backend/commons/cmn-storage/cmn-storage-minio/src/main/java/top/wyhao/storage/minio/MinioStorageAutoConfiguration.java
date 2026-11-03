package top.wyhao.storage.minio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

/**
 * MinIO / RustFS 存储自动配置
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioStorageProperties.class)
public class MinioStorageAutoConfiguration {

    private final MinioStorageProperties properties;

    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "MINIO")
    public StorageService minioStorageService() {
        MinioStorageProperties.S3Config cfg = properties.getMinio();
        validate(cfg, "file.minio");
        MinioCompatibleStorageService storage = new MinioCompatibleStorageService(StorageType.MINIO);
        storage.init(cfg.getEndpoint(), cfg.getAccessKey(), cfg.getSecretKey(), cfg.getBucket());
        log.info("已注册 MinIO 存储: storageType={}, endpoint={}, bucket={}",
                StorageType.MINIO, cfg.getEndpoint(), cfg.getBucket());
        return storage;
    }

    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "RUSTFS")
    public StorageService rustfsStorageService() {
        MinioStorageProperties.S3Config cfg = properties.getRustfs();
        validate(cfg, "file.rustfs");
        MinioCompatibleStorageService storage = new MinioCompatibleStorageService(StorageType.RUSTFS);
        storage.init(cfg.getEndpoint(), cfg.getAccessKey(), cfg.getSecretKey(), cfg.getBucket());
        log.info("已注册 RustFS 存储: storageType={}, endpoint={}, bucket={}",
                StorageType.RUSTFS, cfg.getEndpoint(), cfg.getBucket());
        return storage;
    }

    private void validate(MinioStorageProperties.S3Config cfg, String prefix) {
        if (!cfg.isValid()) {
            throw new IllegalStateException(
                    "存储配置不完整，请检查：" + prefix + ".endpoint / access-key / secret-key / bucket");
        }
    }
}
