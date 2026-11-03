package top.wyhao.tenant.client;

import lombok.Data;

/**
 * 租户信息（跨模块契约）
 */
@Data
public class TenantBO {

    private Long id;

    private String name;

    private String code;

    private String adminUsername;

    private String adminPassword;

    /** 已有用户作为管理员时使用；与 adminUsername/adminPassword 二选一 */
    private Long adminUserId;

    private Long packageId;
}
