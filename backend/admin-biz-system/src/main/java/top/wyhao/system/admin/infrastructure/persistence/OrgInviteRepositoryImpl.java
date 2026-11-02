package top.wyhao.system.admin.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.domain.gateway.OrgInviteRepository;
import top.wyhao.system.admin.domain.model.OrgInviteStatus;
import top.wyhao.system.admin.domain.model.OrgInvite;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysOrgInviteDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysOrgInviteMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织邀请仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class OrgInviteRepositoryImpl implements OrgInviteRepository {

    private final SysOrgInviteMapper inviteMapper;

    @Override
    public void insert(OrgInvite invite) {
        SysOrgInviteDO data = AdminDoMapper.toDo(invite, SysOrgInviteDO.class);
        inviteMapper.insert(data);
        AdminDoMapper.copyBack(data, invite);
    }

    @Override
    public void updateById(OrgInvite invite) {
        inviteMapper.updateById(AdminDoMapper.toDo(invite, SysOrgInviteDO.class));
    }

    @Override
    public OrgInvite findPendingByTenantAndEmail(Long tenantId, String email) {
        return AdminDoMapper.toDomain(inviteMapper.lambdaQuery()
                .eq(SysOrgInviteDO::getTenantId, tenantId)
                .eq(SysOrgInviteDO::getEmail, email)
                .eq(SysOrgInviteDO::getStatus, OrgInviteStatus.PENDING.getValue())
                .eq(SysOrgInviteDO::getDeleted, 0)
                .one(), OrgInvite.class);
    }

    @Override
    public OrgInvite findByTokenHash(String tokenHash) {
        return AdminDoMapper.toDomain(inviteMapper.lambdaQuery()
                .eq(SysOrgInviteDO::getTokenHash, tokenHash)
                .eq(SysOrgInviteDO::getDeleted, 0)
                .one(), OrgInvite.class);
    }

    @Override
    public List<OrgInvite> listPendingByEmail(String email, LocalDateTime now) {
        return AdminDoMapper.toDomainList(inviteMapper.lambdaQuery()
                .eq(SysOrgInviteDO::getEmail, email)
                .eq(SysOrgInviteDO::getStatus, OrgInviteStatus.PENDING.getValue())
                .eq(SysOrgInviteDO::getDeleted, 0)
                .gt(SysOrgInviteDO::getExpireTime, now)
                .list(), OrgInvite.class);
    }
}
