package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.cmn.core.model.RoleDataScope;
import top.wyhao.security.client.LoginContextApi;
import top.wyhao.system.admin.app.service.LoginContextAppService;

import java.util.List;

/**
 * 登录会话角色数据权限 API 适配：只委托 app。
 */
@Service
@RequiredArgsConstructor
public class LoginContextApiImpl implements LoginContextApi {

    private final LoginContextAppService loginContextAppService;

    @Override
    public List<RoleDataScope> listRoleDataScopes(Long userId, Long departmentId) {
        return loginContextAppService.listRoleDataScopes(userId, departmentId);
    }
}
