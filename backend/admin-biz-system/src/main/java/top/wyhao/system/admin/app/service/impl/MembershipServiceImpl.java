package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.system.admin.app.service.MembershipService;
import top.wyhao.system.admin.domain.exception.OrgException;
import top.wyhao.system.admin.domain.gateway.MembershipRepository;
import top.wyhao.system.admin.domain.model.MembershipScopeType;
import top.wyhao.system.admin.domain.model.MembershipStatus;
import top.wyhao.system.admin.domain.model.Membership;
import top.wyhao.system.admin.domain.service.MembershipRules;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePrimaryDepartment(Long userId, Long departmentId) {
        if (userId == null) {
            return;
        }
        membershipRepository.softDeleteByUserIdAndScopeType(userId, MembershipScopeType.DEPARTMENT);
        if (departmentId == null) {
            return;
        }
        membershipRepository.insert(Membership.primaryDepartment(userId, departmentId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePrimaryDepartments(List<long[]> userDepartmentPairs) {
        if (CollUtil.isEmpty(userDepartmentPairs)) {
            return;
        }
        List<Long> userIds = new ArrayList<>(userDepartmentPairs.size());
        List<Membership> toInsert = new ArrayList<>(userDepartmentPairs.size());
        for (long[] pair : userDepartmentPairs) {
            if (pair == null || pair.length < 2) {
                continue;
            }
            long userId = pair[0];
            long departmentId = pair[1];
            userIds.add(userId);
            toInsert.add(Membership.primaryDepartment(userId, departmentId));
        }
        membershipRepository.softDeleteByUserIdsAndScopeType(userIds, MembershipScopeType.DEPARTMENT);
        membershipRepository.insertBatch(toInsert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            return;
        }
        membershipRepository.softDeleteByUserIdAndScopeType(userId, MembershipScopeType.ROLE);
        List<Long> ids = CollUtil.emptyIfNull(roleIds).stream().distinct().toList();
        if (ids.isEmpty()) {
            return;
        }
        membershipRepository.insertBatch(CollUtils.mapToList(ids, roleId -> Membership.role(userId, roleId)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoleMembers(Long roleId, Collection<Long> userIds) {
        if (roleId == null || CollUtil.isEmpty(userIds)) {
            return;
        }
        Set<Long> existing = new HashSet<>(membershipRepository.listActiveUserIdsByScope(MembershipScopeType.ROLE, roleId));
        List<Membership> toInsert = userIds.stream()
                .filter(userId -> userId != null && !existing.contains(userId))
                .distinct()
                .map(userId -> Membership.role(userId, roleId))
                .toList();
        membershipRepository.insertBatch(toInsert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleMembers(Long roleId, Collection<Long> userIds) {
        if (roleId == null || CollUtil.isEmpty(userIds)) {
            return;
        }
        membershipRepository.softDeleteByScopeAndUserIds(MembershipScopeType.ROLE, roleId, userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllRolesByUserIds(Collection<Long> userIds) {
        membershipRepository.softDeleteByUserIdsAndScopeType(userIds, MembershipScopeType.ROLE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllByUserIds(Collection<Long> userIds) {
        membershipRepository.softDeleteByUserIds(userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureTenantMembership(Long userId, Long tenantId, boolean primary) {
        if (userId == null || tenantId == null) {
            return;
        }
        Membership existing = membershipRepository.findActive(userId, MembershipScopeType.TENANT, tenantId);
        if (existing != null) {
            if (existing.getStatus() == null || existing.getStatus() != MembershipStatus.NORMAL.getValue()) {
                existing.setStatus(MembershipStatus.NORMAL.getValue());
                existing.setJoinedAt(LocalDateTime.now());
                membershipRepository.update(existing);
            }
            if (primary && !Boolean.TRUE.equals(existing.getIsPrimary())) {
                setPrimaryTenant(userId, tenantId);
            }
            return;
        }
        if (primary || membershipRepository.findPrimary(userId, MembershipScopeType.TENANT) == null) {
            membershipRepository.clearPrimary(userId, MembershipScopeType.TENANT);
            membershipRepository.insert(Membership.primaryTenant(userId, tenantId));
        } else {
            membershipRepository.insert(Membership.tenant(userId, tenantId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPrimaryTenant(Long userId, Long tenantId) {
        Membership membership = membershipRepository.findActive(userId, MembershipScopeType.TENANT, tenantId);
        if (membership == null || !MembershipRules.isEffective(membership, LocalDateTime.now())) {
            throw OrgException.notMember();
        }
        membershipRepository.clearPrimary(userId, MembershipScopeType.TENANT);
        membership.setIsPrimary(true);
        membershipRepository.update(membership);
    }

    @Override
    public Long resolvePrimaryTenantId(Long userId) {
        if (userId == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        Membership primary = membershipRepository.findPrimary(userId, MembershipScopeType.TENANT);
        if (MembershipRules.isEffective(primary, now)) {
            return primary.getScopeId();
        }
        return listEffectiveTenantMemberships(userId).stream()
                .map(Membership::getScopeId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Membership> listEffectiveTenantMemberships(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return membershipRepository.listByUserAndScopeType(userId, MembershipScopeType.TENANT).stream()
                .filter(m -> MembershipRules.isEffective(m, now))
                .toList();
    }

    @Override
    public boolean isTenantMember(Long userId, Long tenantId) {
        Membership membership = membershipRepository.findActive(userId, MembershipScopeType.TENANT, tenantId);
        return MembershipRules.isEffective(membership, LocalDateTime.now());
    }
}
