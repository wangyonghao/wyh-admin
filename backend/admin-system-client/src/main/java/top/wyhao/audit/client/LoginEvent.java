package top.wyhao.audit.client;

import java.time.LocalDateTime;

/**
 * 登录认证结果事件。Auth 发布，System 监听落库；Auth 不关心存哪、怎么展示。
 */
public final class LoginEvent {

    private final String username;
    private final String ipAddress;
    private final String userAgent;
    private final boolean success;
    private final String failureReason;
    private final Long tenantId;
    private final LocalDateTime loginTime;

    private LoginEvent(String username,
                       String ipAddress,
                       String userAgent,
                       boolean success,
                       String failureReason,
                       Long tenantId,
                       LocalDateTime loginTime) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.success = success;
        this.failureReason = failureReason;
        this.tenantId = tenantId;
        this.loginTime = loginTime;
    }

    public static LoginEvent success(String username, String ipAddress, String userAgent, Long tenantId) {
        return new LoginEvent(username, ipAddress, userAgent, true, null, tenantId, LocalDateTime.now());
    }

    public static LoginEvent failure(String username,
                                     String ipAddress,
                                     String userAgent,
                                     String failureReason,
                                     Long tenantId) {
        return new LoginEvent(username, ipAddress, userAgent, false, failureReason, tenantId, LocalDateTime.now());
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
