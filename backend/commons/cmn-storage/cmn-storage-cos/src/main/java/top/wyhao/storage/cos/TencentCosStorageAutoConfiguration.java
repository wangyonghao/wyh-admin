package top.wyhao.storage.cos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

/**
 * 腾讯云 COS 存储自动配置
 *
 * <p>激活条件：{@code file.storage.type=TENCENT_COS}
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(TencentCosStorageProperties.class)
@ConditionalOnProperty(name = "file.storage.type", havingValue = "TENCENT_COS")
public class TencentCosStorageAutoConfiguration {

    private final TencentCosStorageProperties properties;

    @Bean
    public StorageService tencentCosStorageService() {
        if (!properties.isValid()) {
            throw new IllegalStateException(
                    "腾讯云 COS 配置不完整，请检查：file.cos.secret-id / secret-key / region / bucket");
        }
        TencentCosStorageService storage = new TencentCosStorageService();
        storage.init(properties.getSecretId(), properties.getSecretKey(),
                properties.getRegion(), properties.getBucket());
        log.info("已注册腾讯云 COS 存储: storageType={}, region={}, bucket={}",
                StorageType.TENCENT_COS, properties.getRegion(), properties.getBucket());
        return storage;
    }
}
