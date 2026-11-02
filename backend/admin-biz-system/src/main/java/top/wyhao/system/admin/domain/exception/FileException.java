package top.wyhao.system.admin.domain.exception;

import lombok.Getter;

/**
 * 文件领域异常
 *
 * @author wyh
 * @since 2026/09/16
 */
@Getter
public class FileException extends RuntimeException {

    public static final String CODE_ERROR = "FILE_ERROR";
    public static final String CODE_NOT_FOUND = "FILE_NOT_FOUND";
    public static final String CODE_TYPE_NOT_ALLOWED = "FILE_TYPE_NOT_ALLOWED";

    private final String code;

    public FileException(String message) {
        this(CODE_ERROR, message);
    }

    public FileException(String code, String message) {
        super(message);
        this.code = code;
    }

    public FileException(String message, Throwable cause) {
        this(CODE_ERROR, message, cause);
    }

    public FileException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static FileException of(String message) {
        return new FileException(message);
    }

    public static FileException of(String code, String message) {
        return new FileException(code, message);
    }

    public static FileException notFound(Long fileId) {
        return of(CODE_NOT_FOUND, String.format("文件不存在: %d", fileId));
    }

    public static FileException notFound(String storageKey) {
        return of(CODE_NOT_FOUND, String.format("文件不存在: %s", storageKey));
    }

    public static FileException notFound(Long fileId, Throwable cause) {
        return new FileException(CODE_NOT_FOUND, String.format("文件不存在: %d", fileId), cause);
    }

    public static FileException extensionNotAllowed(String extension) {
        return of(CODE_TYPE_NOT_ALLOWED, String.format("不支持上传 [%s] 类型的文件", extension));
    }

    public static FileException contentMismatch(String extension) {
        return of(CODE_TYPE_NOT_ALLOWED, String.format("文件内容与扩展名 [%s] 不匹配", extension));
    }

    public static FileException hashFailed(Throwable cause) {
        return new FileException("计算文件哈希值失败", cause);
    }
}
