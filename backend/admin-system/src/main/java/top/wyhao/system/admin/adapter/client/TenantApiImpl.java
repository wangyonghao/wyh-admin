package top.wyhao.system.admin.adapter.client;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.service.TenantService;
import top.wyhao.system.admin.domain.model.Tenant;
import top.wyhao.tenant.client.TenantApi;
import top.wyhao.tenant.client.TenantBO;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantApiImpl implements TenantApi {

    private final TenantService tenantService;

    @Override
    public void bindAdminUser(Long tenantId, Long userId) {
        tenantService.bindAdminUser(tenantId, userId);
    }

    @Override
    public TenantBO get(Long id) {
        return toBo(tenantService.getEntity(id));
    }

    @Override
    public List<TenantBO> listByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return tenantService.listEntitiesByIds(ids).stream().map(this::toBo).toList();
    }

    @Override
    public TenantBO findByCode(String code) {
        return toBo(tenantService.getEntityByCode(code));
    }

    @Override
    public Long createOwnedBy(String name, String description, Long ownerUserId) {
        return tenantService.createOwnedBy(name, description, ownerUserId);
    }

    private TenantBO toBo(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        TenantBO bo = new TenantBO();
        bo.setId(tenant.getId());
        bo.setName(tenant.getName());
        bo.setCode(tenant.getCode());
        bo.setPackageId(tenant.getPackageId());
        bo.setAdminUserId(tenant.getAdminUser());
        bo.setAdminUsername(tenant.getAdminUsername());
        return bo;
    }
}
