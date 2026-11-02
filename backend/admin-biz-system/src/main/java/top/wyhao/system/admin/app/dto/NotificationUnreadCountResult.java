package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读公告数量响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "未读公告数量响应参数")
public class NotificationUnreadCountResult {

    @Schema(description = "未读公告数量", example = "1")
    private Integer total;
}
