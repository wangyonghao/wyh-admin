
package top.wyhao.admin.system.model.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.system.model.enums.MessageType;

import java.io.Serial;
import java.io.Serializable;

/**
 * 各类型未读消息响应参数
 *

 * @since 2023/11/2 23:00
 */
@Data
@Schema(description = "各类型未读消息响应参数")
public class MessageUnreadResult {


    /**
     * 类型
     */
    @Schema(description = "类型", example = "1")
    private MessageType type;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "10")
    private Long count;
}