package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.enums.DataScopeEnum;
import top.wyhao.cmn.core.model.RoleDataScope;
import top.wyhao.identity.client.DepartmentApi;
import top.wyhao.system.admin.app.service.LoginContextAppService;
import top.wyhao.system.admin.domain.gateway.RoleDepartmentRepository;
import top.wyhao.system.admin.domain.gateway.RoleRepository;
import top.wyhao.system.admin.domain.model.Role;
import top.wyhao.system.admin.domain.model.RoleDataScopeRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginContextAppServiceImpl implements LoginContextAppService {

    private final RoleRepository roleRepository;
    private final RoleDepartmentRepository roleDepartmentRepository;
    private final DepartmentApi departmentApi;

    @Override
    public List<RoleDataScope> listRoleDataScopes(Long userId, Long departmentId) {
        List<Role> roles = roleRepository.selectRolesByUserId(userId);
        if (CollUtil.isEmpty(roles)) {
            return Collections.emptyList();
        }
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
