package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.service.RoleService;
import top.wyhao.security.client.RoleApi;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色业务 API 实现
 */
@Service
@RequiredArgsConstructor
public class RoleApiImpl implements RoleApi {

    private final RoleService roleService;

    @Override
    public Long getIdByCode(String code) {
        return roleService.getIdByCode(code);
    }

    @Override
    public void updateUserContext(Long roleId) {
        roleService.updateUserContext(roleId);
    }

    @Override
    public int countByNames(List<String> roleNames) {
        return roleService.countByNames(roleNames);
    }

    @Override
    public Map<String, Long> mapIdByNames(List<String> roleNames) {
        return roleService.listByNames(roleNames).stream()
                .collect(Collectors.toMap(r -> r.getName(), r -> r.getId(), (a, b) -> a));
    }

    @Override
    public boolean assignRolesToUser(List<Long> roleIds, Long userId) {
        return roleService.assignRolesToUser(roleIds, userId);
    }

    @Override
    public List<Long> listMemberIds(Long roleId) {
        return roleService.listMemberIds(roleId);
    }

    @Override
    public void deleteUserRolesByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        roleService.deleteUserRolesByUserIds(List.copyOf(userIds));
    }
}
