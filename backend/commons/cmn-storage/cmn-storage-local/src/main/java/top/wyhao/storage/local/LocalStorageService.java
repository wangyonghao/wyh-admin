package top.wyhao.storage.local;

import lombok.extern.slf4j.Slf4j;
import top.wyhao.storage.api.StorageException;
import top.wyhao.storage.api.StorageObject;
import top.wyhao.storage.api.StorageService;
import top.wyhao.storage.api.StorageType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件系统存储实现
 *
 * <p>所有文件路径均在 rootPath 下，防止 Path Traversal；生产多实例不建议用本地磁盘做共享存储。
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
public class LocalStorageService implements StorageService {

    private final LocalStorageProperties properties;

    public LocalStorageService(LocalStorageProperties properties) {
        this.properties = properties;
        File rootDir = new File(properties.getRootPath());
        if (!rootDir.exists()) {
            boolean created = rootDir.mkdirs();
            if (created) {
                log.info("创建本地存储根目录: {}", properties.getRootPath());
            }
        }
    }

    @Override
    public StorageType type() {
        return StorageType.LOCAL;
    }

    @Override
    public void put(String key, InputStream inputStream, long contentLength, String contentType) {
        Path path = resolve(key);
        try {
            Files.createDirectories(path.getParent());
            Files.copy(inputStream, path);
            log.debug("文件写入成功: key={}, path={}", key, path);
        } catch (IOException e) {
            throw new StorageException(StorageType.LOCAL, "写入文件失败: " + key, e);
        }
    }

    @Override
    public StorageObject get(String key) {
        Path path = resolve(key);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return StorageObject.of(new FileInputStream(path.toFile()), Files.size(path), null);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void delete(String key) {
        Path path = resolve(key);
        try {
            Files.deleteIfExists(path);
            log.debug("文件删除成功: key={}", key);
        } catch (IOException e) {
            log.warn("文件删除失败: key={}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        return Files.exists(resolve(key));
    }

    private Path resolve(String key) {
        if (key == null || key.isBlank()) {
            throw new StorageException(StorageType.LOCAL, "存储键不能为空");
        }
        if (key.contains("..") || key.contains("\\") || key.startsWith("/")) {
            throw new StorageException(StorageType.LOCAL, "非法的存储键: " + key);
        }

        Path root = Paths.get(properties.getRootPath()).normalize().toAbsolutePath();
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new StorageException(StorageType.LOCAL, "路径穿越攻击拦截: " + key);
        }
        return target;
    }
}
