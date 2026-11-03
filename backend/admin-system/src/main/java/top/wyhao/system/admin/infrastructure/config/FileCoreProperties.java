package top.wyhao.system.admin.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.wyhao.storage.api.StorageType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 文件核心配置属性
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileCoreProperties {

    /**
     * 存储配置
     */
    private StorageConfig storage = new StorageConfig();

    /**
     * 上传配置
     */
    private UploadConfig upload = new UploadConfig();

    @Data
    public static class UploadConfig {
        /**
         * 允许上传的扩展名（小写，不含点）
         *
         * <p>默认不包含 svg、html 等可在浏览器中执行脚本的类型
         */
        private Set<String> allowedExtensions = new LinkedHashSet<>(List.of(
                "jpg", "jpeg", "png", "gif", "bmp", "webp", "ico",
                "txt", "md", "doc", "docx", "rtf", "odt",
                "xls", "xlsx", "csv", "ods",
                "ppt", "pptx", "odp", "pdf",
                "zip", "rar", "7z", "tar", "gz", "tgz", "bz2",
                "mp3", "wav", "flac", "aac", "ogg", "m4a",
                "mp4", "avi", "mov", "mkv", "wmv", "flv", "webm"));
    }

    @Data
    public static class StorageConfig {
        /**
         * 默认存储类型
         */
        private StorageType type = StorageType.LOCAL;

        /**
         * 存储键前缀（可空）
         *
         * <p>最终 key 格式：[{key-prefix}/]{yyyy}/{MM}/{dd}/{uuid}[.ext]
         * <p>示例配置 {@code files} → {@code files/2026/09/18/abc123.pdf}
         */
        private String keyPrefix = "files";
    }
}
