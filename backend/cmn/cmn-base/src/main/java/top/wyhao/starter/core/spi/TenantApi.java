
package top.wyhao.starter.core.spi;

/**
 * 租户业务 API
 * 
 * @author Charles7c
 * @since 2025/7/23 21:13
 */
public interface TenantApi {

    /**
     * 绑定租户管理员用户
     *
     * @param tenantId 租户 ID
     * @param userId   用户 ID
     */
    void bindAdminUser(Long tenantId, Long userId);
}
