package top.wyhao.system.admin.domain;

/**
 * 平台默认租户保留，不能从租户列表删掉。
 */
public final class TenantLifecycleRules {

    private TenantLifecycleRules() {
    }

    public static boolean deletable(Long tenantId, Long defaultTenantId) {
        return tenantId != null && !tenantId.equals(defaultTenantId);
    }
}
