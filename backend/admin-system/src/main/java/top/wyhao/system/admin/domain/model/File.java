package top.wyhao.system.admin.domain.model;

import java.time.LocalDateTime;


import lombok.Data;
import top.wyhao.system.admin.domain.enums.FileStatus;
import top.wyhao.storage.api.StorageType;

/**
 * 文件实体
 *
 * <p>FILE 表只描述物理文件，不包含业务附件或网盘字段。
 *
 * <p>禁止在此表中增加：BUSINESS_TYPE、BUSINESS_ID、SPACE_ID、FOLDER_ID 等业务字段。
 * 这些字段属于上层业务领域（BusinessFile、FileItem）。
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
public class File {

    private Long id;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Long updateUser;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 文件名（原始文件名）
     */
    private String fileName;

    /**
     * 内容类型（MIME Type）
     */
    private String contentType;

    /**
     * 文件大小（字节数）
     */
    private Long fileSize;

    /**
     * SHA-256 哈希值（用于文件完整性校验和去重）
     */
    private String sha256;

    /**
     * 存储类型
     */
    private StorageType storageType;

    /**
     * 存储键（全局唯一，不使用原始文件名）
     *
     * <p>格式：[{key-prefix}/]{yyyy}/{MM}/{dd}/{uuid}[.ext]
     * <p>前缀由 {@code file.storage.key-prefix} 配置
     */
    private String storageKey;

    /**
     * 文件状态
     */
    private FileStatus status;

    /**
     * 创建文件实体（上传时使用）
     *
     * @param fileName     文件名
     * @param contentType  内容类型
     * @param fileSize     文件大小
     * @param sha256       SHA-256 哈希值
     * @param storageType  存储类型
     * @param storageKey   存储键
     * @return 文件实体
     */
    public static File create(String fileName, String contentType, Long fileSize,
                              String sha256, StorageType storageType, String storageKey) {
        File file = new File();
        file.setFileName(fileName);
        file.setContentType(contentType);
        file.setFileSize(fileSize);
        file.setSha256(sha256);
        file.setStorageType(storageType);
        file.setStorageKey(storageKey);
        file.setStatus(FileStatus.ACTIVE);
        return file;
    }

    /**
     * 标记为已删除（逻辑删除）
     */
    public void markDeleted() {
        this.status = FileStatus.DELETED;
    }

    /**
     * 标记为已清除（物理删除标记）
     */
    public void markPurged() {
        this.status = FileStatus.PURGED;
    }

    /**
     * 是否可访问
     *
     * @return 是否可访问
     */
    public boolean isAccessible() {
        return FileStatus.ACTIVE.equals(this.status);
    }
}
