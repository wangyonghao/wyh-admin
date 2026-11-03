package top.wyhao.storage.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储类型枚举
 *
 * @author wyh
 * @since 2026/09/16
 */
@Getter
@AllArgsConstructor
public enum StorageType {

    /** 本地存储 */
    LOCAL(1, "本地存储"),

    /** 阿里云 OSS */
    ALIYUN_OSS(2, "阿里云 OSS"),

    /** MinIO */
    MINIO(3, "MinIO"),

    /** RustFS（兼容 S3 协议） */
    RUSTFS(4, "RustFS"),

    /** 腾讯云 COS */
    TENCENT_COS(5, "腾讯云 COS");

    private final Integer value;

    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param value 存储类型值
     * @return 存储类型枚举；未匹配时返回 {@code null}
     */
    public static StorageType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (StorageType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
