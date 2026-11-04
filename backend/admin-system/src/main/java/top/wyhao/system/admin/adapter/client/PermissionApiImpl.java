package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.security.client.PermissionApi;
import top.wyhao.system.admin.app.service.PermissionAppService;

import java.util.List;

/**
 * 权限 API 适配：只委托 app。
 */
@Component
@RequiredArgsConstructor
public class PermissionApiImpl implements PermissionApi {

    private final PermissionAppService permissionAppService;

    @Override
    public List<String> findUserPermissions(Long userId) {
        return permissionAppService.findUserPermissions(userId);
    }

    @Override
    public List<String> findUserRoles(Long userId) {
        return permissionAppService.findUserRoles(userId);
    }
}
