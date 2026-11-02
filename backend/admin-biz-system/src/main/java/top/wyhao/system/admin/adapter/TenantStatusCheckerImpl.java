package top.wyhao.system.admin.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.system.admin.app.service.TenantService;
import top.wyhao.tenant.spi.TenantStatusChecker;

/**
 * 将业务 TenantService 状态校验接到 commons 租户钩子。
 */
@Component
@RequiredArgsConstructor
public class TenantStatusCheckerImpl implements TenantStatusChecker {

    private final TenantService tenantService;

    @Override
    public void checkStatus(Long tenantId) {
        tenantService.checkStatus(tenantId);
    }
}
