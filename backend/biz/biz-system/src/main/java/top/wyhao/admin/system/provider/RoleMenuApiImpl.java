
package top.wyhao.admin.system.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.entity.SysRoleMenu;
import top.wyhao.admin.system.service.RoleMenuService;
import top.wyhao.starter.core.spi.RoleMenuApi;
import top.wyhao.starter.core.util.CollUtils;

import java.util.List;
import java.util.Set;

/**
 * 角色和菜单关联业务 API 实现
 *
 * @author Charles7c
 * @since 2025/7/26 9:39
 */
@Service
@RequiredArgsConstructor
public class RoleMenuApiImpl implements RoleMenuApi {

    private final RoleMenuService roleMenuService;

    @Override
    public Set<Long> listRoleIdByNotInMenuIds(List<Long> menuIds) {
        List<SysRoleMenu> roleMenuList = roleMenuService.lambdaQuery()
            .select(SysRoleMenu::getRoleId)
            .notIn(SysRoleMenu::getMenuId, menuIds)
            .list();
        return CollUtils.mapToSet(roleMenuList, SysRoleMenu::getRoleId);
    }

    @Override
    public List<Long> listMenuIdByRoleIds(List<Long> roleIds) {
        return roleMenuService.listMenuIdByRoleIds(roleIds);
    }

    @Override
    public void deleteByNotInMenuIds(List<Long> menuIds) {
        roleMenuService.lambdaUpdate().notIn(SysRoleMenu::getMenuId, menuIds).remove();
    }

    @Override
    public boolean add(List<Long> menuIds, Long roleId) {
        return roleMenuService.save(menuIds, roleId);
    }
}
