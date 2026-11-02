package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.MembershipScopeType;
import top.wyhao.system.admin.domain.model.Membership;

import java.util.Collection;
import java.util.List;

/**
 * 成员关系仓储。
 */
public interface MembershipRepository {

    void insert(Membership membership);

    void insertBatch(List<Membership> memberships);

    void softDeleteByUserIdAndScopeType(Long userId, MembershipScopeType scopeType);

    void softDeleteByUserIds(Collection<Long> userIds);

    void softDeleteByUserIdsAndScopeType(Collection<Long> userIds, MembershipScopeType scopeType);

    void softDeleteByScopeAndUserIds(MembershipScopeType scopeType, Long scopeId, Collection<Long> userIds);

    List<Long> listActiveUserIdsByScope(MembershipScopeType scopeType, Long scopeId);

    List<Long> listActiveScopeIdsByUser(Long userId, MembershipScopeType scopeType);

    List<Membership> listByUserAndScopeType(Long userId, MembershipScopeType scopeType);

    Membership findActive(Long userId, MembershipScopeType scopeType, Long scopeId);

    Membership findPrimary(Long userId, MembershipScopeType scopeType);

    void clearPrimary(Long userId, MembershipScopeType scopeType);

    void update(Membership membership);
}
