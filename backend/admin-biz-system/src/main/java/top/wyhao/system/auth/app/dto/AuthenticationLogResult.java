package top.wyhao.system.auth.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.system.auth.domain.authlog.AuthLogStatus;

import java.time.LocalDateTime;

/**
 * 登录日志响应信息
 *
 * @since 2026/5/8
 */
@Data
@Schema(description = "登录日志响应信息")
public class AuthenticationLogResult {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "设备类型", example = "WEB")
    private String deviceType;

    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "地理位置", example = "中国 北京 北京")
    private String location;

    @Schema(description = "浏览器", example = "Chrome")
    private String browser;

    @Schema(description = "操作系统", example = "Windows 10")
    private String os;

    @Schema(description = "登录状态", example = "SUCCESS")
    private AuthLogStatus loginStatus;

    @Schema(description = "登录时间", example = "2026-05-08 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;

    @Schema(description = "失败原因", example = "用户名或密码错误")
    private String failureReason;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}
