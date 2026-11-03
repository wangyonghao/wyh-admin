package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 在线用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "在线用户响应参数")
public class OnlineUserResult {

    @Schema(description = "会话id", example = "1")
    private String sessionId;

    @Schema(description = "Token", example = "ey****J9.ey****fQ.7q****vE")
    private String token;

    @Schema(description = "用户名", example = "zhangsan")
    private String loginName;

    @Schema(description = "登录 IP", example = "")
    private String ip;

    @Schema(description = "登录地点", example = "中国北京北京市")
    private String location;

    @Schema(description = "浏览器", example = "Chrome 115.0.0.0")
    private String browser;

    @Schema(description = "操作系统", example = "Windows 10")
    private String os;

    @Schema(description = "登录时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime loginTime;

    @Schema(description = "最后活跃时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime lastActiveTime;
}
