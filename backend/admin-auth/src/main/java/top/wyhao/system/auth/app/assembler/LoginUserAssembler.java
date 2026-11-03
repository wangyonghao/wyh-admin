package top.wyhao.system.auth.app.assembler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.security.client.LoginContextApi;
import top.wyhao.system.auth.domain.user.CredentialUser;

/**
 * 将身份凭证组装为登录会话用户（角色数据权限委托 Admin {@link LoginContextApi}）。
 */
@Component
@RequiredArgsConstructor
public class LoginUserAssembler {

    private final LoginContextApi loginContextApi;

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
        loginUser.setRoleDataScopes(loginContextApi.listRoleDataScopes(user.id(), user.departmentId()));
        return loginUser;
    }
}
