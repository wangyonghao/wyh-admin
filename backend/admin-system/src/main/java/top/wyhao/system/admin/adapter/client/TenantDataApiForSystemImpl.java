package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.service.TenantDataAppService;
import top.wyhao.tenant.client.TenantBO;
import top.wyhao.tenant.client.TenantDataApi;

/**
 * 租户数据 API 适配：只委托 app 编排。
 */
@Service
@RequiredArgsConstructor
public class TenantDataApiForSystemImpl implements TenantDataApi {

    private final TenantDataAppService tenantDataAppService;

    @Override
    public void init(TenantBO tenant) {
        tenantDataAppService.init(tenant);
    }

    @Override
    public void clear() {
        tenantDataAppService.clear();
    }
}
