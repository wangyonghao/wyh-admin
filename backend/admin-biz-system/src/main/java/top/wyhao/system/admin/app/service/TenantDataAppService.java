package top.wyhao.system.admin.app.service;

import top.wyhao.tenant.client.TenantBO;

/**
 * 租户初始化/清理编排（事务边界在此，不在 adapter/client）。
 */
public interface TenantDataAppService {

    void init(TenantBO tenant);

    void clear();
}
