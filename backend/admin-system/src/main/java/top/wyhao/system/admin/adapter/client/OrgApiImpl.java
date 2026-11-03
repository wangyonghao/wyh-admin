package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.service.OrgService;
import top.wyhao.tenant.client.OrgApi;

@Service
@RequiredArgsConstructor
public class OrgApiImpl implements OrgApi {

    private final OrgService orgService;

    @Override
    public Long resolveLoginTenantId(Long userId) {
        return orgService.resolveLoginTenantId(userId);
    }
}
