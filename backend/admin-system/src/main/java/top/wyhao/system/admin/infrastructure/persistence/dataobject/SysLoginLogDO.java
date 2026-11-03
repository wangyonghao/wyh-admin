package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.tenant.core.TenantedEntity;

import java.time.LocalDateTime;

/**
 * 登录日志。对应表 sys_login_log。
 */
@Data
@TableName("sys_login_log")
public class SysLoginLogDO implements TenantedEntity {

    @TableId
    private Long id;
    private String username;
    private String ipAddress;
    private String location;
    private String deviceType;
    private String browser;
    private String os;
    private String loginStatus;
    private LocalDateTime loginTime;
    private String failureReason;
    private String userAgent;
    private Long tenantId;
}
