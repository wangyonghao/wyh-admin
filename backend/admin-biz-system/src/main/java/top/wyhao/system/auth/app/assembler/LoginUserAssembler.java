package top.wyhao.system.auth.app.assembler;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.cmn.core.enums.DataScopeEnum;
import top.wyhao.cmn.core.model.RoleDataScope;
import top.wyhao.system.auth.domain.user.CredentialUser;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.system.admin.adapter.client.DepartmentApi;
import top.wyhao.system.admin.domain.model.RoleDataScopeRules;
import top.wyhao.system.admin.domain.gateway.RoleDepartmentRepository;
import top.wyhao.system.admin.domain.gateway.RoleRepository;
import top.wyhao.system.admin.domain.model.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 将身份凭证组装为登录会话用户（含角色数据权限范围）。
 */
@Component
@RequiredArgsConstructor
public class LoginUserAssembler {

    private final RoleRepository roleRepository;
    private final RoleDepartmentRepository roleDepartmentRepository;
    private final DepartmentApi departmentApi;

    public LoginUser assemble(CredentialUser user) {
        return assemble(user, null);
    }

    public LoginUser assemble(CredentialUser user, String deviceType) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.id());
        loginUser.setUsername(user.username());
        loginUser.setDepartmentId(user.departmentId());
        loginUser.setPwdResetTime(user.pwdUpdateTime());
        loginUser.setDeviceType(deviceType);
        loginUser.setRoleDataScopes(buildRoleDataScopes(user));
        return loginUser;
    }

    private List<RoleDataScope> buildRoleDataScopes(CredentialUser user) {
        List<Role> roles = roleRepository.selectRolesByUserId(user.id());
        if (CollUtil.isEmpty(roles)) {
            return Collections.emptyList();
        }
        Long departmentId = user.departmentId();
        List<Long> departmentAndDescendants = null;
        List<RoleDataScope> scopes = new ArrayList<>(roles.size());
        for (Role role : roles) {
            DataScopeEnum dataScope = role.getDataScope();
            if (DataScopeEnum.DEPARTMENT_AND_CHILD.equals(dataScope) && departmentAndDescendants == null) {
                departmentAndDescendants = departmentId == null
                        ? Collections.emptyList()
                        : departmentApi.listSelfAndDescendantIds(departmentId);
            }
            RoleDataScope scope = new RoleDataScope();
            scope.setId(role.getId());
            scope.setCode(role.getCode());
            scope.setDataScope(dataScope);
            scope.setVisibleDepartmentIds(RoleDataScopeRules.visibleDepartmentIds(
                    dataScope,
                    departmentId,
                    departmentAndDescendants,
                    dataScope == DataScopeEnum.CUSTOM_DEPARTMENT
                            ? roleDepartmentRepository.listDepartmentIdsByRoleId(role.getId())
                            : null));
            scopes.add(scope);
        }
        return scopes;
    }
}
