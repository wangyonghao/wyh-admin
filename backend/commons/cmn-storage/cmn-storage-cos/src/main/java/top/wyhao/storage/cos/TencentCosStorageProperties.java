package top.wyhao.storage.cos;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 腾讯云 COS 配置属性
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
@ConfigurationProperties(prefix = "file.cos")
public class TencentCosStorageProperties {

    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;

    public boolean isValid() {
        return notBlank(secretId) && notBlank(secretKey)
                && notBlank(region) && notBlank(bucket);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
