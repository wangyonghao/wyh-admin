package top.wyhao.storage.aliyun;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置属性
 *
 * <pre>
 * file:
 *   storage:
 *     type: ALIYUN_OSS
 *   aliyun-oss:
 *     endpoint:   ${FILE_ALIYUN_OSS_ENDPOINT}
 *     access-key: ${FILE_ALIYUN_OSS_ACCESS_KEY}
 *     secret-key: ${FILE_ALIYUN_OSS_SECRET_KEY}
 *     bucket:     ${FILE_ALIYUN_OSS_BUCKET}
 * </pre>
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
@ConfigurationProperties(prefix = "file.aliyun-oss")
public class AliyunOssStorageProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String customDomain;

    public boolean isValid() {
        return notBlank(endpoint) && notBlank(accessKey)
                && notBlank(secretKey) && notBlank(bucket);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
