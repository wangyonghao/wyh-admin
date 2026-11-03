package top.wyhao.storage.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import top.wyhao.storage.api.StorageException;
import top.wyhao.storage.api.StorageObject;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

import java.io.InputStream;

/**
 * 腾讯云 COS 存储实现
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
public class TencentCosStorageService implements StorageService {

    private COSClient cosClient;
    private String bucket;

    public void init(String secretId, String secretKey, String region, String bucket) {
        this.bucket = bucket;
        try {
            this.cosClient = new COSClient(
                    new BasicCOSCredentials(secretId, secretKey),
                    new ClientConfig(new Region(region)));
            log.info("腾讯云 COS 客户端初始化成功: region={}, bucket={}", region, bucket);
        } catch (Exception e) {
            throw new StorageException(StorageType.TENCENT_COS, "COS 客户端初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
            log.info("腾讯云 COS 客户端已关闭");
        }
    }

    @Override
    public StorageType type() {
        return StorageType.TENCENT_COS;
    }

    @Override
    public void put(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            ObjectMetadata meta = new ObjectMetadata();
            if (contentLength > 0) {
                meta.setContentLength(contentLength);
            }
            if (contentType != null) {
                meta.setContentType(contentType);
            }
            cosClient.putObject(new PutObjectRequest(bucket, key, inputStream, meta));
            log.debug("上传到腾讯云 COS 成功: key={}", key);
        } catch (Exception e) {
            throw new StorageException(StorageType.TENCENT_COS, "上传文件失败: " + key, e);
        }
    }

    @Override
    public StorageObject get(String key) {
        try {
            COSObject obj = cosClient.getObject(bucket, key);
            if (obj == null) {
                return null;
            }
            return StorageObject.of(
                    obj.getObjectContent(),
                    obj.getObjectMetadata().getContentLength(),
                    obj.getObjectMetadata().getContentType()
            );
        } catch (Exception e) {
            log.debug("从腾讯云 COS 获取文件失败: key={}", key, e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            cosClient.deleteObject(bucket, key);
            log.debug("从腾讯云 COS 删除文件成功: key={}", key);
        } catch (Exception e) {
            log.warn("从腾讯云 COS 删除文件失败: key={}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return cosClient.doesObjectExist(bucket, key);
        } catch (Exception e) {
            log.error("检查腾讯云 COS 文件是否存在失败: key={}", key, e);
            return false;
        }
    }
}
