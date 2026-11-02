package top.wyhao.system.admin.app.service;

import java.util.List;
import java.util.Set;

/**
 * 角色-菜单关联编排（供 client API 委托）。
 */
public interface RoleMenuAppService {

    Set<Long> listRoleIdsNotInMenuIds(List<Long> menuIds);

    void deleteMenuIdNotIn(List<Long> menuIds);

    boolean replaceByRoleId(Long roleId, List<Long> menuIds);
}
