package top.wyhao.system.admin.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.service.RoleMenuAppService;
import top.wyhao.system.admin.domain.gateway.RoleMenuRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleMenuAppServiceImpl implements RoleMenuAppService {

    private final RoleMenuRepository roleMenuRepository;

    @Override
    public Set<Long> listRoleIdsNotInMenuIds(List<Long> menuIds) {
        return roleMenuRepository.listRoleIdsNotInMenuIds(menuIds);
    }

    @Override
    public void deleteMenuIdNotIn(List<Long> menuIds) {
        roleMenuRepository.deleteMenuIdNotIn(menuIds);
    }

    @Override
    public boolean replaceByRoleId(Long roleId, List<Long> menuIds) {
        return roleMenuRepository.replaceByRoleId(roleId, menuIds);
    }
}
