package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.system.admin.domain.model.MessageType;

/**
 * 各类型未读消息数量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "各类型未读消息数量")
public class MessageUnreadCountResult {

    @Schema(description = "类型", example = "1")
    private MessageType type;

    @Schema(description = "数量", example = "10")
    private Long count;
}
