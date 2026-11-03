package top.wyhao.tenant.client;

import java.util.Collection;
import java.util.List;

/**
 * 租户业务 API
 */
public interface TenantApi {

    /**
     * 绑定租户管理员用户
     */
    void bindAdminUser(Long tenantId, Long userId);

    TenantBO get(Long id);

    List<TenantBO> listByIds(Collection<Long> ids);

    TenantBO findByCode(String code);

    /**
     * 当前用户自助创建组织（租户），并以该用户为管理员。
     *
     * @return 新组织 ID
     */
    Long createOwnedBy(String name, String description, Long ownerUserId);
}
