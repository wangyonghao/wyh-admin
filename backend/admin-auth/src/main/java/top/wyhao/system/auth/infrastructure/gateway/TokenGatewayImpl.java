package top.wyhao.system.auth.infrastructure.gateway;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.cmn.core.util.IpUtils;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.system.auth.app.assembler.LoginUserAssembler;
import top.wyhao.system.auth.domain.gateway.TokenGateway;
import top.wyhao.system.auth.domain.gateway.UserGateway;
import top.wyhao.system.auth.domain.model.ClientContext;
import top.wyhao.system.auth.domain.user.CredentialUser;

/**
 * 管理 AccessToken/OTP 的签发
 */
@Component
@RequiredArgsConstructor
public class TokenGatewayImpl implements TokenGateway {

    private final LoginUserAssembler loginUserAssembler;
    private final UserGateway userGateway;

    @Override
    public String issue(CredentialUser user, ClientContext clientContext) {
        String location = IpUtils.getRegion(clientContext.ip());
        UserAgent ua = UserAgentUtil.parse(clientContext.userAgent());
        String browser = ua != null ? ua.getBrowser().getName() : "Unknown";
        String os = ua != null ? ua.getOs().getName() : "Unknown";

        StpUtil.login(user.id());

        LoginUser loginUser = loginUserAssembler.assemble(user, "PC");
        SaSession session = StpUtil.getTokenSession();
        session.set("loginName", user.username());
        session.set("ipaddr", clientContext.ip());
        session.set("loginLocation", location);
        session.set("browser", browser);
        session.set("os", os);
        session.set("loginTime", System.currentTimeMillis());
        session.set("deviceType", "PC");
        session.set(LoginUtil.LOGIN_USER_KEY, loginUser);

        // 会话租户取主组织 / 超管默认租户，不能用 departmentId
        Long tenantId = userGateway.resolveLoginTenantId(user.id());
        LoginUtil.setTenantId(tenantId);

        return LoginUtil.getTokenValue();
    }

    @Override
    public void logoutIfLoggedIn() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }
}
