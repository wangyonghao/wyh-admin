package top.wyhao.admin.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.system.model.enums.LoginStatusEnum;

import java.time.LocalDateTime;

/**
 * 登录日志查询条件
 *

 * @since 2026/05/08
 */
@Data
@Schema(description = "登录日志查询条件")
public class LoginLogQuery {

    /**
     * 用户名（模糊查询）
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * IP地址（模糊查询）
     */
    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    /**
     * 登录状态
     */
    @Schema(description = "登录状态", example = "SUCCESS")
    private LoginStatusEnum loginStatus;

    /**
     * 登录开始时间
     */
    @Schema(description = "登录开始时间", example = "2026-01-01T00:00:00")
    private LocalDateTime loginTimeStart;

    /**
     * 登录结束时间
     */
    @Schema(description = "登录结束时间", example = "2026-12-31T23:59:59")
    private LocalDateTime loginTimeEnd;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}
