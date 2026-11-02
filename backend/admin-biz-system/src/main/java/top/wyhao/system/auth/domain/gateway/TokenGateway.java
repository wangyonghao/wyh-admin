package top.wyhao.system.auth.domain.gateway;

import top.wyhao.system.auth.domain.model.ClientContext;
import top.wyhao.system.auth.domain.user.CredentialUser;

/**
 * Token / Session 签发（Sa-Token 等基础设施适配）。
 * Sa-Token 采用自动续期（Active-Timeout）机制，只要用户在 activity-timeout 时间内有操作，Token 的活跃时间就会自动续签，不需要额外调用刷新接口
 */
public interface TokenGateway {

    /**
     * 签发登录态并绑定会话，返回成功结果。
     */
    String issue(CredentialUser user, ClientContext clientContext);

    /**
     * 若已登录则登出（第三方登录前清理）。
     */
    void logoutIfLoggedIn();
}
