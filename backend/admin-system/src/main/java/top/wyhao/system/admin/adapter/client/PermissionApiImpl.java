package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.cmn.core.enums.RoleCodeEnum;
import top.wyhao.security.client.PermissionApi;
import top.wyhao.system.admin.domain.gateway.MenuRepository;
import top.wyhao.system.admin.domain.gateway.UserRoleRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionApiImpl implements PermissionApi {

    private final UserRoleRepository userRoleRepository;
    private final MenuRepository menuRepository;

    @Override
    public List<String> findUserPermissions(Long userId) {
        List<String> roleCodes = findUserRoles(userId);
        if (roleCodes.contains(RoleCodeEnum.SUPER_ADMIN.getCode())) {
            return List.of("*:*:*");
        }
        return menuRepository.listPermissionCodesByUserId(userId);
    }

    @Override
    public List<String> findUserRoles(Long userId) {
        return userRoleRepository.listRoleCodesByUserId(userId);
    }
}
