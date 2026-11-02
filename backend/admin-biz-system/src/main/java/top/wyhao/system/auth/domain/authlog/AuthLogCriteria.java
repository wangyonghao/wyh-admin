package top.wyhao.system.auth.domain.authlog;

import java.time.LocalDateTime;

/**
 * 登录日志查询条件。租户范围由应用层决定后写入。
 */
public class AuthLogCriteria {

    private String username;
    private String ipAddress;
    private String loginStatus;
    private LocalDateTime loginTimeStart;
    private LocalDateTime loginTimeEnd;
    private Long tenantId;
    private boolean tenantRestricted;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public LocalDateTime getLoginTimeStart() {
        return loginTimeStart;
    }

    public void setLoginTimeStart(LocalDateTime loginTimeStart) {
        this.loginTimeStart = loginTimeStart;
    }

    public LocalDateTime getLoginTimeEnd() {
        return loginTimeEnd;
    }

    public void setLoginTimeEnd(LocalDateTime loginTimeEnd) {
        this.loginTimeEnd = loginTimeEnd;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isTenantRestricted() {
        return tenantRestricted;
    }

    public void setTenantRestricted(boolean tenantRestricted) {
        this.tenantRestricted = tenantRestricted;
    }
}
