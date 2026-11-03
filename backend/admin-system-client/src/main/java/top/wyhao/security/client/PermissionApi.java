package top.wyhao.security.client;

import java.util.List;

/**
 * 授权端口：解析用户被授予的角色码与权限码。
 */
public interface PermissionApi {
    /**
     * 获取用户权限码集合
     *
     * @param userId 用户ID
     * @return 权限码集合
     */
    List<String> findUserPermissions(Long userId);

    /**
     * 获取用户角色码集合
     *
     * @param userId 用户ID
     * @return 角色码集合
     */
    List<String> findUserRoles(Long userId);
}
