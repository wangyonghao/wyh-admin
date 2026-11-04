package top.wyhao.system.admin.app.service;

import java.util.List;

/**
 * 用户权限/角色编排（供 client API 委托）。
 */
public interface PermissionAppService {

    List<String> findUserPermissions(Long userId);

    List<String> findUserRoles(Long userId);
}
