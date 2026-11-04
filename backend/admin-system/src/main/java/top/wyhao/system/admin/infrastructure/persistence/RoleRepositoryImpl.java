package top.wyhao.system.admin.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.domain.gateway.RoleRepository;
import top.wyhao.system.admin.domain.model.Role;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysRoleMapper;

import java.util.Collection;
import java.util.List;

/**
 * 角色仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final SysRoleMapper roleMapper;

    @Override
    public Role findById(Long id) {
        return AdminDoMapper.toDomain(roleMapper.selectById(id), Role.class);
    }

    @Override
    public int insert(Role role) {
        SysRoleDO data = AdminDoMapper.toDo(role, SysRoleDO.class);
        int rows = roleMapper.insert(data);
        AdminDoMapper.copyBack(data, role);
        return rows;
    }

    @Override
    public int updateById(Role role) {
        SysRoleDO data = AdminDoMapper.toDo(role, SysRoleDO.class);
        int rows = roleMapper.updateById(data);
        AdminDoMapper.copyBack(data, role);
        return rows;
    }

    @Override
    public void deleteById(Long id) {
        roleMapper.deleteById(id);
    }

    @Override
    public boolean nameExists(String name, Long selfId) {
        return roleMapper.isNameExists(name, selfId);
    }

    @Override
    public void updateMenuCheckStrictly(Long roleId, Boolean menuCheckStrictly) {
        roleMapper.lambdaUpdate()
                .set(SysRoleDO::getMenuCheckStrictly, menuCheckStrictly)
                .eq(SysRoleDO::getId, roleId)
                .update();
    }

    @Override
    public Long findIdByCode(String code) {
        return roleMapper.lambdaQuery().eq(SysRoleDO::getCode, code).oneOpt().map(SysRoleDO::getId).orElse(null);
    }

    @Override
    public List<Role> listByNames(List<String> names) {
        return AdminDoMapper.toDomainList(
                roleMapper.selectList(Wrappers.<SysRoleDO>lambdaQuery().in(SysRoleDO::getName, names)),
                Role.class);
    }

    @Override
    public int countByNames(List<String> names) {
        return roleMapper.selectCount(Wrappers.<SysRoleDO>lambdaQuery().in(SysRoleDO::getName, names)).intValue();
    }

    @Override
    public void deleteAll() {
        roleMapper.delete(Wrappers.<SysRoleDO>query().eq("1", 1));
    }

    @Override
    public long countExcluding(Collection<Long> keepIds) {
        return roleMapper.lambdaQuery().notIn(SysRoleDO::getId, keepIds).count();
    }

    @Override
    public boolean deleteExcluding(Collection<Long> keepIds) {
        return roleMapper.lambdaUpdate().notIn(SysRoleDO::getId, keepIds).remove();
    }

    @Override
    public List<Role> selectRolesByUserId(Long userId) {
        return AdminDoMapper.toDomainList(roleMapper.selectRolesByUserId(userId), Role.class);
    }
}
