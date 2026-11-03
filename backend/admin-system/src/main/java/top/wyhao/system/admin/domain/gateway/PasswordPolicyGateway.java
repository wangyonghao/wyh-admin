package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.PasswordPolicy;

/**
 * 当前生效的密码策略（领域侧读取口）。
 */
public interface PasswordPolicyGateway {

    /**
     * @return 领域密码策略快照；实现须在配置缺失时给出安全默认值
     */
    PasswordPolicy current();
}
