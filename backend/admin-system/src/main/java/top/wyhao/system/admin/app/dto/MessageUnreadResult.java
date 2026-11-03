package top.wyhao.system.admin.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 未读消息响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "未读消息响应参数")
public class MessageUnreadResult {

    @Schema(description = "未读消息数量", example = "20")
    private Long total;

    @Schema(description = "各类型未读消息数量")
    private List<MessageUnreadCountResult> details;
}
