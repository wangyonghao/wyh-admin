package top.wyhao.storage.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import top.wyhao.storage.api.StorageException;
import top.wyhao.storage.api.StorageObject;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

import java.io.InputStream;

/**
 * 阿里云 OSS 存储实现
 *
 * <p>不向上层暴露 OSS SDK 类型；密钥由自动配置从环境注入。
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
public class AliyunOssStorageService implements StorageService {

    private OSS ossClient;
    private String bucket;

    /**
     * 初始化 OSS 客户端
     *
     * @param endpoint  服务端点
     * @param accessKey Access Key ID
     * @param secretKey Access Key Secret
     * @param bucket    Bucket 名称
     */
    public void init(String endpoint, String accessKey, String secretKey, String bucket) {
        this.bucket = bucket;
        try {
            this.ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
            log.info("阿里云 OSS 客户端初始化成功: endpoint={}, bucket={}", endpoint, bucket);
        } catch (Exception e) {
            throw new StorageException(StorageType.ALIYUN_OSS, "OSS 客户端初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("阿里云 OSS 客户端已关闭");
        }
    }

    @Override
    public StorageType type() {
        return StorageType.ALIYUN_OSS;
    }

    @Override
    public void put(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            ossClient.putObject(bucket, key, inputStream);
            log.debug("上传到阿里云 OSS 成功: bucket={}, key={}", bucket, key);
        } catch (Exception e) {
            throw new StorageException(StorageType.ALIYUN_OSS, "上传文件失败: " + key, e);
        }
    }

    @Override
    public StorageObject get(String key) {
        try {
            OSSObject obj = ossClient.getObject(bucket, key);
            if (obj == null) {
                return null;
            }
            return StorageObject.of(
                    obj.getObjectContent(),
                    obj.getObjectMetadata().getContentLength(),
                    obj.getObjectMetadata().getContentType()
            );
        } catch (Exception e) {
            log.debug("从阿里云 OSS 获取文件失败: key={}", key, e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            ossClient.deleteObject(bucket, key);
            log.debug("从阿里云 OSS 删除文件成功: key={}", key);
        } catch (Exception e) {
            log.warn("从阿里云 OSS 删除文件失败: key={}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return ossClient.doesObjectExist(bucket, key);
        } catch (Exception e) {
            log.error("检查阿里云 OSS 文件是否存在失败: key={}", key, e);
            return false;
        }
    }
}
