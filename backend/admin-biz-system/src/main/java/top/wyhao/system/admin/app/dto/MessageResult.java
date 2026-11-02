package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.system.admin.domain.model.MessageType;
import top.wyhao.system.admin.domain.model.NoticeScopes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息响应参数
 *
 * @since 2023/10/15 19:05
 */
@Data
@Schema(description = "消息响应参数")
public class MessageResult {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "标题", example = "欢迎注册 xxx")
    private String title;

    @Schema(description = "内容", example = "尊敬的 xx，欢迎注册使用，请及时配置您的密码。")
    private String content;

    @Schema(description = "类型", example = "1")
    private MessageType type;

    @Schema(description = "跳转路径", example = "/user/profile")
    private String path;

    @Schema(description = "通知范围", example = "2")
    private NoticeScopes scope;

    @Schema(description = "通知用户", example = "[1,2]")
    private List<String> users;

    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;

    @Schema(description = "是否已读", example = "true")
    private Boolean isRead;
}
