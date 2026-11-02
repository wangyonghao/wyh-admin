package top.wyhao.system.admin.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.domain.gateway.OrgApplicationRepository;
import top.wyhao.system.admin.domain.model.OrgApplicationStatus;
import top.wyhao.system.admin.domain.model.OrgApplication;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysOrgApplicationDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysOrgApplicationMapper;

/**
 * 组织申请仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class OrgApplicationRepositoryImpl implements OrgApplicationRepository {

    private final SysOrgApplicationMapper applicationMapper;

    @Override
    public void insert(OrgApplication application) {
        SysOrgApplicationDO data = AdminDoMapper.toDo(application, SysOrgApplicationDO.class);
        applicationMapper.insert(data);
        AdminDoMapper.copyBack(data, application);
    }

    @Override
    public void updateById(OrgApplication application) {
        applicationMapper.updateById(AdminDoMapper.toDo(application, SysOrgApplicationDO.class));
    }

    @Override
    public OrgApplication findById(Long id) {
        return AdminDoMapper.toDomain(applicationMapper.selectById(id), OrgApplication.class);
    }

    @Override
    public boolean existsPending(Long tenantId, Long userId) {
        return applicationMapper.lambdaQuery()
                .eq(SysOrgApplicationDO::getTenantId, tenantId)
                .eq(SysOrgApplicationDO::getUserId, userId)
                .eq(SysOrgApplicationDO::getStatus, OrgApplicationStatus.PENDING.getValue())
                .eq(SysOrgApplicationDO::getDeleted, 0)
                .exists();
    }
}
