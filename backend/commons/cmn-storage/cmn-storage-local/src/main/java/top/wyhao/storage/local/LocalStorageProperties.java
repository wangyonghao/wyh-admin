package top.wyhao.storage.local;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地存储配置属性
 *
 * <pre>
 * file:
 *   storage:
 *     type: LOCAL
 *   local:
 *     root-path: /data/itas/files
 * </pre>
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
@ConfigurationProperties(prefix = "file.local")
public class LocalStorageProperties {

    /**
     * 文件存储根路径；所有物理文件必须位于此路径下
     */
    private String rootPath = "/data/itas/files";
}
