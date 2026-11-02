package top.wyhao.system.auth.domain.gateway;

/**
 * 登录审计日志。用于安全审计、异常登录发现
 */
public interface AuthLogGateway {

    void recordSuccess(String username, String ip, String userAgent);

    /**
     * 记录登录失败日志
     * 真实失败原因写给管理员看, 对前端仍统一返回"用户名或密码错误",避免泄露账号是否存在
     */
    void recordFailure(String username, String ip, String userAgent, String reason);
}
