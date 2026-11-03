package top.wyhao.storage.aliyun;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

/**
 * 阿里云 OSS 存储自动配置
 *
 * <p>激活条件：{@code file.storage.type=ALIYUN_OSS}
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(AliyunOssStorageProperties.class)
@ConditionalOnProperty(name = "file.storage.type", havingValue = "ALIYUN_OSS")
public class AliyunOssStorageAutoConfiguration {

    private final AliyunOssStorageProperties properties;

    @Bean
    public StorageService aliyunOssStorageService() {
        if (!properties.isValid()) {
            throw new IllegalStateException(
                    "阿里云 OSS 配置不完整，请检查：file.aliyun-oss.endpoint / access-key / secret-key / bucket");
        }
        AliyunOssStorageService storage = new AliyunOssStorageService();
        storage.init(properties.getEndpoint(), properties.getAccessKey(),
                properties.getSecretKey(), properties.getBucket());
        log.info("已注册阿里云 OSS 存储: storageType={}, endpoint={}, bucket={}",
                StorageType.ALIYUN_OSS, properties.getEndpoint(), properties.getBucket());
        return storage;
    }
}
