package top.wyhao.system.admin.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 一条待验证的邮箱注册。密码是慢哈希，token 只保留摘要。
 */
@Data
public class EmailRegistration {

    public static final String SEND_PENDING = "PENDING";
    public static final String SEND_SENT = "SENT";
    public static final String SEND_FAILED = "FAILED";

    private Long id;
    private Long tenantId;
    private String email;
    private String username;
    private String passwordHash;
    private String tokenHash;
    private Long departmentId;
    private Long roleId;
    private LocalDateTime expiresAt;
    private LocalDateTime sentAt;
    private String sendStatus;
    private LocalDateTime consumedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public boolean expired(LocalDateTime now) {
        return expiresAt == null || !expiresAt.isAfter(now);
    }

    public boolean consumed() {
        return consumedAt != null;
    }
}
