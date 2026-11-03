package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息查询条件
 *
 * @since 2023/10/15 19:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息查询条件")
public class MessageQuery {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "标题", example = "欢迎注册 xxx")
    private String title;

    @Schema(description = "类型", example = "1")
    private Integer type;

    @Schema(description = "是否已读", example = "true")
    private Boolean isRead;

    @Schema(hidden = true)
    private Long userId;
}
