
package top.wyhao.admin.system.model.vo.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 未读消息响应参数
 *

 * @since 2023/11/2 23:00
 */
@Data
@Schema(description = "未读消息响应参数")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MessageUnreadResp {

    /**
     * 未读消息数量
     */
    @Schema(description = "未读消息数量", example = "20")
    private Long total;

    /**
     * 各类型未读消息数量
     */
    @Schema(description = "各类型未读消息数量")
    private List<MessageUnreadResult> details;
}