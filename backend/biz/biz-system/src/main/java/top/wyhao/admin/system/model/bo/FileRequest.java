
package top.wyhao.admin.system.model.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件请求参数
 *

 * @since 2023/12/23 10:38
 */
@Data
@Schema(description = "文件请求参数")
public class FileRequest{
    @Schema(description = "业务ID", example = "1234567890")
    private Long bizId;
    @Schema(description = "业务类型", example = "user_avatar")
    private String bizType;

    /**
     * 上级目录
     */
    @Schema(description = "上级目录", example = "/")
    private String path;


}