
package top.wyhao.starter.tenant.config;

import top.wyhao.starter.tenant.context.TenantContext;

/**
 * 租户提供者
 *


 * @since 2.7.0
 */
public interface TenantProvider {

    /**
     * 根据租户 ID 获取租户上下文
     *
     * @param tenantIdAsString 租户 ID 字符串
     * @param checkStatus         是否验证有效性
     * @return 租户上下文
     */
    TenantContext getByTenantId(String tenantIdAsString, boolean checkStatus);
}
