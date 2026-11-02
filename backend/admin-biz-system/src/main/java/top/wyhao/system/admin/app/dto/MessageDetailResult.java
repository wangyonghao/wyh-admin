package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.system.admin.domain.model.MessageType;

import java.time.LocalDateTime;

/**
 * 消息详情响应参数
 */
@Data
@Schema(description = "消息详情响应参数")
public class MessageDetailResult {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "标题", example = "欢迎注册 xxx")
    private String title;

    @Schema(description = "类型", example = "1")
    private MessageType type;

    @Schema(description = "跳转路径", example = "/user/profile")
    private String path;

    @Schema(description = "是否已读", example = "true")
    private Boolean isRead;

    @Schema(description = "读取时间", example = "2023-08-08 23:59:59", type = "string")
    private LocalDateTime readTime;

    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;
}
