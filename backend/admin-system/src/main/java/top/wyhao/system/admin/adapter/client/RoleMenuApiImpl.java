package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.security.client.RoleMenuApi;
import top.wyhao.system.admin.app.service.RoleMenuAppService;

import java.util.List;
import java.util.Set;

/**
 * 角色和菜单关联业务 API 适配：只委托 app。
 */
@Service
@RequiredArgsConstructor
public class RoleMenuApiImpl implements RoleMenuApi {

    private final RoleMenuAppService roleMenuAppService;

    @Override
    public Set<Long> listRoleIdByNotInMenuIds(List<Long> menuIds) {
        return roleMenuAppService.listRoleIdsNotInMenuIds(menuIds);
    }

    @Override
    public void deleteByNotInMenuIds(List<Long> menuIds) {
        roleMenuAppService.deleteMenuIdNotIn(menuIds);
    }

    @Override
    public boolean add(List<Long> menuIds, Long roleId) {
        return roleMenuAppService.replaceByRoleId(roleId, menuIds);
    }
}
