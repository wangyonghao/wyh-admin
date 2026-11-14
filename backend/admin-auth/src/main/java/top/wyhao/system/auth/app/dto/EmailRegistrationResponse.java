package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱注册已受理。没有登录 token。delivered 为 false 时表示这次没有新的邮件被接受。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "邮箱注册受理结果")
public class EmailRegistrationResponse {

    @Schema(description = "成功码", example = "200")
    private String code;

    @Schema(description = "PENDING 已提交发送；COOLDOWN 仍在重发间隔内", example = "PENDING")
    private String status;

    @Schema(description = "邮件服务器是否已接受这封信。未发送或仍在间隔内为 false")
    private boolean delivered;

    @Schema(description = "距离允许下一次发送的秒数")
    private int resendAfterSeconds;

    @Schema(description = "链接有效分钟数")
    private int expiresMinutes;
}
