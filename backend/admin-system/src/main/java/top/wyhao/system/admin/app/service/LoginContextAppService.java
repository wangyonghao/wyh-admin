package top.wyhao.system.admin.app.service;

import top.wyhao.cmn.core.model.RoleDataScope;

import java.util.List;

/**
 * 登录会话角色数据权限编排（供 client API 委托）。
 */
public interface LoginContextAppService {

    List<RoleDataScope> listRoleDataScopes(Long userId, Long departmentId);
}
