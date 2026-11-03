package top.wyhao.storage.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO / RustFS 存储配置属性
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
@ConfigurationProperties(prefix = "file")
public class MinioStorageProperties {

    private S3Config minio = new S3Config();
    private S3Config rustfs = new S3Config();

    @Data
    public static class S3Config {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket;

        public boolean isValid() {
            return notBlank(endpoint) && notBlank(accessKey)
                    && notBlank(secretKey) && notBlank(bucket);
        }

        private static boolean notBlank(String s) {
            return s != null && !s.isBlank();
        }
    }
}
