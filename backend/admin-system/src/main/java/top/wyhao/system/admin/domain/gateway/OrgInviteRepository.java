package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.OrgInvite;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织邀请仓储。
 */
public interface OrgInviteRepository {

    void insert(OrgInvite invite);

    void updateById(OrgInvite invite);

    OrgInvite findPendingByTenantAndEmail(Long tenantId, String email);

    OrgInvite findByTokenHash(String tokenHash);

    List<OrgInvite> listPendingByEmail(String email, LocalDateTime now);
}
