package top.wyhao.admin.system.provider;

import com.alicp.jetcache.anno.support.ConfigProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.starter.core.model.OSSConfig;
import top.wyhao.starter.core.spi.OSSConfigProvider;

@Component
@RequiredArgsConstructor
public class OSSConfigProviderImpl implements OSSConfigProvider {
    private final ConfigProvider configProvider;

    @Override
    public OSSConfig getOSSConfig(String key) {

        return null;
    }

    @Override
    public String getStorageProvider() {
        return "local";
    }

    @Override
    public String getStorageLocalPath() {
        return "./data/file";
    }

    @Override
    public String getStorageEndPoint() {
        return "/api/fs/"; // 通过 FileAccessController 来访问本地文件
    }

    @Override
    public String getStorageMinioEndpoint() {
        return "";
    }

    @Override
    public String getStorageMinioAccessKey() {
        return "";
    }

    @Override
    public String getStorageMinioSecretKey() {
        return "";
    }

    @Override
    public String getStorageMinioBucket() {
        return "";
    }

    @Override
    public String getStorageRustfsEndpoint() {
        return "";
    }

    @Override
    public String getStorageRustfsAccessKey() {
        return "";
    }

    @Override
    public String getStorageRustfsSecretKey() {
        return "";
    }

    @Override
    public String getStorageRustfsBucket() {
        return "";
    }

    @Override
    public String getStorageAliyunEndpoint() {
        return "";
    }

    @Override
    public String getStorageAliyunAccessKey() {
        return "";
    }

    @Override
    public String getStorageAliyunSecretKey() {
        return "";
    }

    @Override
    public String getStorageAliyunBucket() {
        return "";
    }

    @Override
    public String getStorageTencentSecretId() {
        return "";
    }

    @Override
    public String getStorageTencentSecretKey() {
        return "";
    }

    @Override
    public String getStorageTencentRegion() {
        return "";
    }

    @Override
    public String getStorageTencentBucket() {
        return "";
    }
}
