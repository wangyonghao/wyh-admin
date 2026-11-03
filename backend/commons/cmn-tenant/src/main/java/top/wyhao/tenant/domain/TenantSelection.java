package top.wyhao.tenant.domain;

import top.wyhao.tenant.exception.TenantException;

/**
 * 选择本次请求使用的租户。
 *
 * <p>已登录会话上的租户优先于请求头，避免持有 A 租户令牌的人用 {@code X-Tenant-Id} 读到 B 租户的数据。
 * 未登录时才看请求头；都没有则落到平台默认租户。
 */
public final class TenantSelection {

    private TenantSelection() {
    }

    public static Long select(Long sessionTenantId, String headerTenantId, Long defaultTenantId) {
        if (sessionTenantId != null) {
            return sessionTenantId;
        }
        if (headerTenantId != null && !headerTenantId.isBlank()) {
            try {
                return Long.valueOf(headerTenantId.trim());
            } catch (NumberFormatException ex) {
                throw TenantException.invalidId();
            }
        }
        return defaultTenantId;
    }
}
