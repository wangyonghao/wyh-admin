package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.system.admin.domain.enums.FileStatus;
import top.wyhao.storage.api.StorageType;

import java.time.LocalDateTime;

/**
 * 文件响应
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件响应")
public class FileResponse {

    /**
     * 文件 ID
     */
    @Schema(description = "文件 ID")
    private Long id;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private String contentType;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /**
     * SHA-256 哈希值
     */
    @Schema(description = "SHA-256 哈希值")
    private String sha256;

    /**
     * 存储类型
     */
    @Schema(description = "存储类型")
    private StorageType storageType;

    /**
     * 文件状态
     */
    @Schema(description = "文件状态")
    private FileStatus status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
