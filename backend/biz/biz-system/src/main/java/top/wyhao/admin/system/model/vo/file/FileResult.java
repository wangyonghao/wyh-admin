
package top.wyhao.admin.system.model.vo.file;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.system.model.enums.FileType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件响应参数
 *

 * @since 2023/12/23 10:38
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "文件响应参数")
public class FileResult implements Serializable {
    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;
    @Schema(description = "文件名称", example = "example.jpg")
    private String fileName;
    @Schema(description = "OSS中的文件名称", example = "6824afe8408da079832dcfb6.jpg")
    private String ossFileName;
    @Schema(description = "大小（字节）", example = "4096")
    private Long fileSize;

    /**
     * URL
     */
    @Schema(description = "URL", example = "https://examplebucket.oss-cn-hangzhou.aliyuncs.com/2025/2/25/6824afe8408da079832dcfb6.jpg")
    private String ossUrl;

    /**
     * 路径
     */
    @Schema(description = "路径", example = "/2025/2/25/6824afe8408da079832dcfb6.jpg")
    private String ossPath;

    /**
     * 扩展名
     */
    @Schema(description = "扩展名", example = "jpg")
    private String fileExtension;

    /**
     * 内容类型
     */
    @Schema(description = "内容类型", example = "image/jpeg")
    private String contentType;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    private FileType type;

    /**
     * SHA256 值
     */
    @Schema(description = "SHA256 值", example = "722f185c48bed892d6fa12e2b8bf1e5f8200d4a70f522fb62112b6caf13cb74e")
    private String sha256;

    /**
     * 元数据
     */
    @Schema(description = "元数据", example = "{width:1024,height:1024}")
    private String metadata;

    /**
     * 缩略图名称
     */
    @Schema(description = "缩略图名称", example = "example.jpg.min.jpg")
    private String thumbnailName;

    /**
     * 缩略图大小（字节)
     */
    @Schema(description = "缩略图大小（字节)", example = "1024")
    private Long thumbnailSize;

    /**
     * 缩略图元数据
     */
    @Schema(description = "缩略图文件元数据", example = "{width:100,height:100}")
    private String thumbnailMetadata;

    /**
     * 缩略图 URL
     */
    @Schema(description = "缩略图 URL", example = "https://examplebucket.oss-cn-hangzhou.aliyuncs.com/2025/2/25/example.jpg.min.jpg")
    private String thumbnailUrl;

    /**
     * 存储 ID
     */
    @Schema(description = "存储 ID", example = "1")
    private Long storageId;

    /**
     * 存储名称
     */
    @Schema(description = "存储名称", example = "MinIO")
    private String storageName;

    private Long createUser;
    @Schema(description = "创建人", example = "超级管理员")
    private String createUserString;
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    /**
     * 修改人
     */
    @JsonIgnore
    private Long updateUser;

    /**
     * 修改人
     */
    @Schema(description = "修改人", example = "李四")
    private String updateUserString;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime updateTime;


}