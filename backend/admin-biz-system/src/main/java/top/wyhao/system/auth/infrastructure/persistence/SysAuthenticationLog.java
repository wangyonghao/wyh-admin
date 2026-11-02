package top.wyhao.system.auth.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.tenant.core.TenantedEntity;

import java.time.LocalDateTime;

/**
 * 认证日志。对应表 sys_login_log。
 */
@Data
@TableName("sys_login_log")
public class SysAuthenticationLog implements TenantedEntity {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态
     */
    private String loginStatus;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 失败原因（登录失败时记录）
     */
    private String failureReason;

    /**
     * User-Agent
     */
    private String userAgent;

    private Long tenantId;
}
