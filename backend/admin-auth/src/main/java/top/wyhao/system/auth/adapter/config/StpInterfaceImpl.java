
package top.wyhao.system.auth.adapter.config;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import top.wyhao.security.client.PermissionApi;

import java.util.List;

/**
 * 获取用户角色和权限
 *

 */
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionApi permissionProvider;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return permissionProvider.findUserPermissions(Long.parseLong(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return permissionProvider.findUserRoles(Long.parseLong(loginId.toString()));
    }
}
