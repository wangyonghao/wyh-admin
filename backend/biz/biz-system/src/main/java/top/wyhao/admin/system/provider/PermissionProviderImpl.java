package top.wyhao.admin.system.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.admin.system.mapper.SysMenuMapper;
import top.wyhao.admin.system.mapper.SysUserMapper;
import top.wyhao.admin.system.service.UserService;
import top.wyhao.starter.core.enums.RoleCodeEnum;
import top.wyhao.starter.core.spi.PermissionProvider;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionProviderImpl implements PermissionProvider {
    private final UserService userService;

    @Override
    public List<String> findUserPermissions(Long userId) {
        return userService.findUserPermissions(userId);
    }

    @Override
    public List<String> findUserRoles(Long userId) {
        return userService.findUserRoles(userId);
    }

}
