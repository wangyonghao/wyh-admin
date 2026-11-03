package top.wyhao.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import top.wyhao.storage.api.StorageException;
import top.wyhao.storage.api.StorageObject;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

import java.io.InputStream;

/**
 * MinIO / RustFS 通用存储实现（兼容 S3 协议）
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
public class MinioCompatibleStorageService implements StorageService {

    private static final int PART_SIZE = 10 * 1024 * 1024;

    private final StorageType storageType;
    private MinioClient minioClient;
    private String bucket;

    public MinioCompatibleStorageService(StorageType storageType) {
        this.storageType = storageType;
    }

    public void init(String endpoint, String accessKey, String secretKey, String bucket) {
        this.bucket = bucket;
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("{} Bucket 创建成功: {}", storageType, bucket);
            }
            log.info("{} 客户端初始化成功: endpoint={}, bucket={}", storageType, endpoint, bucket);
        } catch (Exception e) {
            throw new StorageException(storageType, storageType + " 客户端初始化失败", e);
        }
    }

    @Override
    public StorageType type() {
        return storageType;
    }

    @Override
    public void put(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(inputStream, contentLength, PART_SIZE)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
            log.debug("上传到 {} 成功: key={}", storageType, key);
        } catch (Exception e) {
            throw new StorageException(storageType, "上传文件失败: " + key, e);
        }
    }

    @Override
    public StorageObject get(String key) {
        try {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(key).build());
            String contentLengthHeader = response.headers().get("Content-Length");
            long length = contentLengthHeader != null ? Long.parseLong(contentLengthHeader) : -1L;
            return StorageObject.of(response, length, response.headers().get("Content-Type"));
        } catch (ErrorResponseException e) {
            log.debug("{} 文件不存在: key={}", storageType, key);
            return null;
        } catch (Exception e) {
            log.debug("从 {} 获取文件失败: key={}", storageType, key, e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(key).build());
            log.debug("从 {} 删除文件成功: key={}", storageType, key);
        } catch (Exception e) {
            log.warn("从 {} 删除文件失败: key={}", storageType, key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(key).build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            log.error("检查 {} 文件是否存在失败: key={}", storageType, key, e);
            return false;
        }
    }
}
