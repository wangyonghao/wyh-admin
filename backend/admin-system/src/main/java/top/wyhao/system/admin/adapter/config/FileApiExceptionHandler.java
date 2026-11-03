package top.wyhao.system.admin.adapter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.wyhao.system.admin.domain.exception.FileException;
import top.wyhao.storage.api.StorageException;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件 API 异常处理器
 *
 * @author wyh
 * @since 2026/09/16
 */
@Slf4j
@Order(50)
@RestControllerAdvice(basePackages = "top.wyhao.admin.system.file.adapter")
public class FileApiExceptionHandler {

    @ExceptionHandler(FileException.class)
    public ResponseEntity<Map<String, Object>> handleFileException(FileException e) {
        HttpStatus status = switch (e.getCode()) {
            case FileException.CODE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FileException.CODE_TYPE_NOT_ALLOWED -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        if (status.is5xxServerError()) {
            log.error("文件操作异常: {}", e.getMessage(), e);
        } else {
            log.warn("文件操作异常: code={}, msg={}", e.getCode(), e.getMessage());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", e.getCode());
        result.put("msg", e.getMessage());
        return ResponseEntity.status(status).body(result);
    }

    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleStorageException(StorageException e) {
        log.error("存储操作异常: {}", e.getMessage(), e);
        Map<String, Object> result = new HashMap<>();
        result.put("code", e.getCode());
        result.put("msg", e.getMessage());
        return result;
    }
}
