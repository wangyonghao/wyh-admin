package top.wyhao.security.client;

import top.wyhao.cmn.core.model.RoleDataScope;

import java.util.List;

/**
 * 登录会话上下文：角色与数据权限范围（由 Admin 计算）。
 */
public interface LoginContextApi {

    /**
     * 按用户与所属部门组装角色数据权限。
     */
    List<RoleDataScope> listRoleDataScopes(Long userId, Long departmentId);
}
