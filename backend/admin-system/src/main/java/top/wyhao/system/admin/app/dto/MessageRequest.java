package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import top.wyhao.system.admin.domain.model.MessageType;

/**
 * 消息创建请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息创建请求参数")
public class MessageRequest {



    @Schema(description = "标题", example = "欢迎注册 xxx")
    @NotBlank(message = "标题不能为空")
    @Length(max = 50, message = "标题长度不能超过 {max} 个字符")
    private String title;

    @Schema(description = "内容", example = "尊敬的 xx，欢迎注册使用，请及时配置您的密码。")
    @NotBlank(message = "内容不能为空")
    @Length(max = 255, message = "内容长度不能超过 {max} 个字符")
    private String content;

    @Schema(description = "类型", example = "1")
    @NotNull(message = "类型无效")
    private MessageType type;

    @Schema(description = "跳转路径", example = "/user/profile")
    private String path;
}
