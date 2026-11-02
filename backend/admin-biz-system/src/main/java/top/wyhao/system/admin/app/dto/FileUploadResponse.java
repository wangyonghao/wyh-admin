package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应
 *
 * @author wyh
 * @since 2026/09/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传响应")
public class FileUploadResponse {

    /**
     * 文件 ID
     */
    @Schema(description = "文件 ID")
    private Long fileId;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private String contentType;

    /**
     * 创建上传响应
     *
     * @param fileId      文件 ID
     * @param fileName    文件名
     * @param fileSize    文件大小
     * @param contentType 内容类型
     * @return 上传响应
     */
    public static FileUploadResponse of(Long fileId, String fileName, Long fileSize, String contentType) {
        return new FileUploadResponse(fileId, fileName, fileSize, contentType);
    }
}
