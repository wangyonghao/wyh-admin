package top.wyhao.admin.system.model.vo.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.system.model.enums.LoginStatusEnum;

import java.time.LocalDateTime;

/**
 * 登录日志响应信息
 *

 * @since 2026/5/8
 */
@Data
@Schema(description = "登录日志响应信息")
public class LoginLogResult {
    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;
    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "WEB")
    private String deviceType;

    /**
     * IP地址
     */
    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    /**
     * 地理位置
     */
    @Schema(description = "地理位置", example = "中国 北京 北京")
    private String location;


    /**
     * 浏览器
     */
    @Schema(description = "浏览器", example = "Chrome")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统", example = "Windows 10")
    private String os;

    /**
     * 登录状态
     */
    @Schema(description = "登录状态", example = "SUCCESS")
    private LoginStatusEnum loginStatus;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2026-05-08 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因", example = "用户名或密码错误")
    private String failureReason;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}
