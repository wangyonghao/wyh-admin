package top.wyhao.system.auth.domain.gateway;

/**
 * 登录审计端口。Auth 只发事件，不关心日志存哪、怎么展示。
 */
public interface AuthLogGateway {

    void recordSuccess(String username, String ip, String userAgent);

    /**
     * 记录登录失败（真实原因供管理员审计；对前端仍统一模糊提示）。
     */
    void recordFailure(String username, String ip, String userAgent, String reason);
}
