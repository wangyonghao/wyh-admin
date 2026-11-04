package top.wyhao.system.admin.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.enums.RoleCodeEnum;
import top.wyhao.system.admin.app.service.PermissionAppService;
import top.wyhao.system.admin.domain.gateway.MenuRepository;
import top.wyhao.system.admin.domain.gateway.UserRoleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionAppServiceImpl implements PermissionAppService {

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
