package top.wyhao.system.admin.infrastructure.persistence;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.system.admin.domain.gateway.RoleMenuRepository;
import top.wyhao.system.admin.domain.model.RoleMenu;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleMenuDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysRoleMenuMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 角色与菜单关联仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class RoleMenuRepositoryImpl implements RoleMenuRepository {

    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<Long> listMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.lambdaQuery()
                .select(SysRoleMenuDO::getMenuId)
                .eq(SysRoleMenuDO::getRoleId, roleId)
                .list()
                .stream()
                .map(SysRoleMenuDO::getMenuId)
                .toList();
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        roleMenuMapper.lambdaUpdate().eq(SysRoleMenuDO::getRoleId, roleId).remove();
    }

    @Override
    public void deleteByMenuIds(Collection<Long> menuIds) {
        if (CollUtil.isEmpty(menuIds)) {
            return;
        }
        roleMenuMapper.lambdaUpdate().in(SysRoleMenuDO::getMenuId, menuIds).remove();
    }

    @Override
    public boolean insertBatch(List<RoleMenu> roleMenus) {
        return roleMenuMapper.insertBatch(AdminDoMapper.toDoList(roleMenus, SysRoleMenuDO.class));
    }

    @Override
    public boolean replaceByRoleId(Long roleId, List<Long> menuIds) {
        List<Long> ids = CollUtil.emptyIfNull(menuIds);
        List<Long> oldIds = listMenuIdsByRoleId(roleId);
        if (CollUtil.isEmpty(CollUtil.disjunction(ids, oldIds))) {
            return false;
        }
        deleteByRoleId(roleId);
        if (CollUtil.isEmpty(ids)) {
            return true;
        }
        List<RoleMenu> roleMenus = CollUtils.mapToList(ids, menuId -> new RoleMenu(roleId, menuId));
        return insertBatch(roleMenus);
    }

    @Override
    public List<Long> listMenuIdsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        return roleMenuMapper.selectMenuIdByRoleIds(roleIds);
    }

    @Override
    public Set<Long> listRoleIdsNotInMenuIds(List<Long> menuIds) {
        List<SysRoleMenuDO> roleMenuList = roleMenuMapper.lambdaQuery()
                .select(SysRoleMenuDO::getRoleId)
                .notIn(SysRoleMenuDO::getMenuId, menuIds)
                .list();
        return CollUtils.mapToSet(roleMenuList, SysRoleMenuDO::getRoleId);
    }

    @Override
    public void deleteMenuIdNotIn(List<Long> menuIds) {
        roleMenuMapper.lambdaUpdate().notIn(SysRoleMenuDO::getMenuId, menuIds).remove();
    }

    @Override
    public void deleteAll() {
        roleMenuMapper.delete(Wrappers.<SysRoleMenuDO>query().eq("1", 1));
    }

    @Override
    public boolean deleteByRoleIdNotIn(Collection<Long> keepRoleIds) {
        return roleMenuMapper.lambdaUpdate().notIn(SysRoleMenuDO::getRoleId, keepRoleIds).remove();
    }
}
