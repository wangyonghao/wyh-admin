package top.wyhao.system.auth.domain.gateway;

import top.wyhao.system.auth.domain.user.PasswordPolicy;

/**
 * 登录安全：失败重试计数、口令比对、登录策略配置。
 */
public interface PasswordFailGateway {
    enum LimitDimension {ACCOUNT, IP}

    int getFailCount(String account);

    int increment(String account, String ip, int durationMinutes);

    /**
     * 清理登录失败记录，当密码登录成功后调用
     * @param account 账号，用户名/手机号/邮箱之一
     */
    void clear(String account);

    PasswordPolicy currentPolicy();

    boolean isBanned(LimitDimension demension, String key);

    int getRemainMinutes(LimitDimension demension, String key);
}


