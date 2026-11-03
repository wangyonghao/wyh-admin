package top.wyhao.storage.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

/**
 * 存储对象
 *
 * <p>表示从存储系统中获取的文件对象，包含文件流、内容长度和内容类型。
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageObject {

    /** 文件输入流 */
    private InputStream inputStream;

    /** 内容长度（字节数） */
    private long contentLength;

    /** 内容类型（MIME Type） */
    private String contentType;

    /**
     * 创建存储对象
     *
     * @param inputStream   文件输入流
     * @param contentLength 内容长度
     * @param contentType   内容类型
     * @return 存储对象
     */
    public static StorageObject of(InputStream inputStream, long contentLength, String contentType) {
        return new StorageObject(inputStream, contentLength, contentType);
    }
}
