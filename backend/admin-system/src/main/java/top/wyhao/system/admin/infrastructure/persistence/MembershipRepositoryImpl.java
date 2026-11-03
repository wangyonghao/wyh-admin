package top.wyhao.system.admin.infrastructure.persistence;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.domain.gateway.MembershipRepository;
import top.wyhao.system.admin.domain.model.MembershipScopeType;
import top.wyhao.system.admin.domain.model.Membership;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMembershipDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysMembershipMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 成员关系仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class MembershipRepositoryImpl implements MembershipRepository {

    private final SysMembershipMapper membershipMapper;

    @Override
    public void insert(Membership membership) {
        SysMembershipDO data = AdminDoMapper.toDo(membership, SysMembershipDO.class);
        membershipMapper.insert(data);
        AdminDoMapper.copyBack(data, membership);
    }

    @Override
    public void insertBatch(List<Membership> memberships) {
        if (CollUtil.isEmpty(memberships)) {
            return;
        }
        membershipMapper.insertBatch(AdminDoMapper.toDoList(memberships, SysMembershipDO.class));
    }

    @Override
    public void softDeleteByUserIdAndScopeType(Long userId, MembershipScopeType scopeType) {
        membershipMapper.lambdaUpdate()
                .eq(SysMembershipDO::getUserId, userId)
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getDeleted, 0)
                .set(SysMembershipDO::getDeleted, 1)
                .set(SysMembershipDO::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void softDeleteByUserIds(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        membershipMapper.lambdaUpdate()
                .in(SysMembershipDO::getUserId, userIds)
                .eq(SysMembershipDO::getDeleted, 0)
                .set(SysMembershipDO::getDeleted, 1)
                .set(SysMembershipDO::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void softDeleteByUserIdsAndScopeType(Collection<Long> userIds, MembershipScopeType scopeType) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        membershipMapper.lambdaUpdate()
                .in(SysMembershipDO::getUserId, userIds)
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getDeleted, 0)
                .set(SysMembershipDO::getDeleted, 1)
                .set(SysMembershipDO::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void softDeleteByScopeAndUserIds(MembershipScopeType scopeType, Long scopeId, Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        membershipMapper.lambdaUpdate()
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getScopeId, scopeId)
                .in(SysMembershipDO::getUserId, userIds)
                .eq(SysMembershipDO::getDeleted, 0)
                .set(SysMembershipDO::getDeleted, 1)
                .set(SysMembershipDO::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public List<Long> listActiveUserIdsByScope(MembershipScopeType scopeType, Long scopeId) {
        return membershipMapper.lambdaQuery()
                .select(SysMembershipDO::getUserId)
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getScopeId, scopeId)
                .eq(SysMembershipDO::getDeleted, 0)
                .list()
                .stream()
                .map(SysMembershipDO::getUserId)
                .toList();
    }

    @Override
    public List<Long> listActiveScopeIdsByUser(Long userId, MembershipScopeType scopeType) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return membershipMapper.lambdaQuery()
                .select(SysMembershipDO::getScopeId)
                .eq(SysMembershipDO::getUserId, userId)
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getDeleted, 0)
                .list()
                .stream()
                .map(SysMembershipDO::getScopeId)
                .toList();
    }

    @Override
    public List<Membership> listByUserAndScopeType(Long userId, MembershipScopeType scopeType) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return AdminDoMapper.toDomainList(membershipMapper.lambdaQuery()
                .eq(SysMembershipDO::getUserId, userId)
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getDeleted, 0)
                .list(), Membership.class);
    }

    @Override
    public Membership findActive(Long userId, MembershipScopeType scopeType, Long scopeId) {
        if (userId == null || scopeId == null) {
            return null;
        }
        return AdminDoMapper.toDomain(membershipMapper.lambdaQuery()
                .eq(SysMembershipDO::getUserId, userId)
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getScopeId, scopeId)
                .eq(SysMembershipDO::getDeleted, 0)
                .one(), Membership.class);
    }

    @Override
    public Membership findPrimary(Long userId, MembershipScopeType scopeType) {
        if (userId == null) {
            return null;
        }
        return AdminDoMapper.toDomain(membershipMapper.lambdaQuery()
                .eq(SysMembershipDO::getUserId, userId)
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getIsPrimary, true)
                .eq(SysMembershipDO::getDeleted, 0)
                .one(), Membership.class);
    }

    @Override
    public void clearPrimary(Long userId, MembershipScopeType scopeType) {
        if (userId == null) {
            return;
        }
        membershipMapper.lambdaUpdate()
                .eq(SysMembershipDO::getUserId, userId)
                .eq(SysMembershipDO::getScopeType, scopeType)
                .eq(SysMembershipDO::getIsPrimary, true)
                .eq(SysMembershipDO::getDeleted, 0)
                .set(SysMembershipDO::getIsPrimary, false)
                .set(SysMembershipDO::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void update(Membership membership) {
        membershipMapper.updateById(AdminDoMapper.toDo(membership, SysMembershipDO.class));
    }
}
