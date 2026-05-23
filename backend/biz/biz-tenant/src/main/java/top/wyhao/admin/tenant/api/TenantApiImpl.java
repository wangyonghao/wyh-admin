
package top.wyhao.admin.tenant.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.admin.tenant.constant.TenantConstants;
import top.wyhao.admin.tenant.mapper.SysTenantMapper;
import top.wyhao.admin.tenant.model.entity.Tenant;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.spi.TenantApi;

/**
 * 租户业务 API 实现
 *

 * @since 2025/7/23 21:13
 */
@Service
@RequiredArgsConstructor
public class TenantApiImpl implements TenantApi {

    private final SysTenantMapper baseMapper;

    @Override
    public void bindAdminUser(Long tenantId, Long userId) {
        baseMapper.lambdaUpdate().set(Tenant::getAdminUser, userId).eq(Tenant::getId, tenantId).update();
        // 更新租户缓存
        Tenant entity = baseMapper.selectById(tenantId);
        RedisUtils.set(TenantConstants.TENANT_KEY_PREFIX + tenantId, entity);
    }
}
