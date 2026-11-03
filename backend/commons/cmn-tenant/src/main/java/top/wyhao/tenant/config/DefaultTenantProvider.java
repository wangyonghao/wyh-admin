package top.wyhao.tenant.config;

import lombok.RequiredArgsConstructor;
import top.wyhao.tenant.context.TenantContext;
import top.wyhao.tenant.spi.TenantStatusChecker;

/**
 * 默认租户提供者
 */
@RequiredArgsConstructor
public class DefaultTenantProvider implements TenantProvider {

    private final TenantProperties tenantProperties;
    private final TenantStatusChecker tenantStatusChecker;

    @Override
    public TenantContext getByTenantId(String tenantIdAsString, boolean checkStatus) {
        TenantContext context = new TenantContext();
        Long defaultTenantId = tenantProperties.getDefaultTenantId();
        context.setTenantId(defaultTenantId);
        // 默认租户
        if (defaultTenantId.toString().equals(tenantIdAsString)) {
            return context;
        }
        Long tenantId = Long.parseLong(tenantIdAsString);
        // 检查租户状态
        if (checkStatus) {
            tenantStatusChecker.checkStatus(tenantId);
        }
        context.setTenantId(tenantId);
        return context;
    }
}
