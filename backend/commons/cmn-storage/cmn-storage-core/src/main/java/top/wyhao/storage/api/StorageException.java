package top.wyhao.storage.api;

import lombok.Getter;

/**
 * 存储异常
 *
 * @author wyh
 * @since 2026/09/16
 */
@Getter
public class StorageException extends RuntimeException {

    private final String code;

    public StorageException(String message) {
        super(message);
        this.code = "STORAGE_ERROR";
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.code = "STORAGE_ERROR";
    }

    public StorageException(StorageType storageType, String message) {
        super(String.format("[%s] %s", storageType.getDescription(), message));
        this.code = "STORAGE_ERROR";
    }

    public StorageException(StorageType storageType, String message, Throwable cause) {
        super(String.format("[%s] %s", storageType.getDescription(), message), cause);
        this.code = "STORAGE_ERROR";
    }

}
