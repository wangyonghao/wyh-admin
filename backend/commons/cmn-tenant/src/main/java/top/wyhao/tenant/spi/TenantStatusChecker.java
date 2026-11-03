package top.wyhao.tenant.spi;

/**
 * 租户状态校验钩子。业务侧（admin-biz-system）实现；commons 不依赖业务模块。
 */
public interface TenantStatusChecker {

    /**
     * 校验租户可用（未禁用、未过期等）；不可用时抛业务异常。
     */
    void checkStatus(Long tenantId);
}
