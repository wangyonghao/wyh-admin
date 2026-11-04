package top.wyhao.system.admin.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.domain.gateway.UserRoleRepository;
import top.wyhao.system.admin.domain.model.UserRole;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysUserRoleDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysUserRoleMapper;

import java.util.Collection;
import java.util.List;

/**
 * 用户与角色关联仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final SysUserRoleMapper userRoleMapper;

    @Override
    public boolean insertBatch(List<UserRole> userRoles) {
        return userRoleMapper.insertBatch(AdminDoMapper.toDoList(userRoles, SysUserRoleDO.class));
    }

    @Override
    public boolean existsByRoleId(Long roleId) {
        return userRoleMapper.lambdaQuery().eq(SysUserRoleDO::getRoleId, roleId).exists();
    }

    @Override
    public List<UserRole> listByUserId(Long userId) {
        return AdminDoMapper.toDomainList(
                userRoleMapper.lambdaQuery()
                        .select(SysUserRoleDO::getRoleId)
                        .eq(SysUserRoleDO::getUserId, userId)
                        .list(),
                UserRole.class);
    }

    @Override
    public List<Long> listUserIdsByRoleId(Long roleId) {
        return userRoleMapper.lambdaQuery()
                .select(SysUserRoleDO::getUserId)
                .eq(SysUserRoleDO::getRoleId, roleId)
                .list()
                .stream()
                .map(SysUserRoleDO::getUserId)
                .toList();
    }

    @Override
    public void deleteByUserId(Long userId) {
        userRoleMapper.lambdaUpdate().eq(SysUserRoleDO::getUserId, userId).remove();
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        userRoleMapper.lambdaUpdate().in(SysUserRoleDO::getUserId, userIds).remove();
    }

    @Override
    public void deleteByRoleIdAndUserIds(Long roleId, List<Long> userIds) {
        userRoleMapper.lambdaUpdate()
                .eq(SysUserRoleDO::getRoleId, roleId)
                .in(SysUserRoleDO::getUserId, userIds)
                .remove();
    }

    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        return userRoleMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public void deleteAll() {
        userRoleMapper.delete(Wrappers.<SysUserRoleDO>query().eq("1", 1));
    }

    @Override
    public boolean deleteByRoleIdNotIn(Collection<Long> keepRoleIds) {
        return userRoleMapper.lambdaUpdate().notIn(SysUserRoleDO::getRoleId, keepRoleIds).remove();
    }

    @Override
    public boolean deleteByUserIdNotIn(Collection<Long> keepUserIds) {
        return userRoleMapper.lambdaUpdate().notIn(SysUserRoleDO::getUserId, keepUserIds).remove();
    }
}
