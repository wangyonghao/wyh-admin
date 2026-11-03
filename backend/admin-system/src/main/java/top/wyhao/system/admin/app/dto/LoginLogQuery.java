package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.system.admin.domain.model.LoginLogStatus;

import java.time.LocalDateTime;

/**
 * 登录日志查询条件
 */
@Data
@Schema(description = "登录日志查询条件")
public class LoginLogQuery {

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "登录状态", example = "SUCCESS")
    private LoginLogStatus loginStatus;

    @Schema(description = "登录开始时间", example = "2026-01-01T00:00:00")
    private LocalDateTime loginTimeStart;

    @Schema(description = "登录结束时间", example = "2026-12-31T23:59:59")
    private LocalDateTime loginTimeEnd;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}
