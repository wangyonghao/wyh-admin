package top.wyhao.system.admin.infrastructure.gateway;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.identity.client.PasswordPolicyConfig;
import top.wyhao.identity.client.PasswordPolicyConfigApi;
import top.wyhao.system.admin.domain.gateway.PasswordPolicyGateway;
import top.wyhao.system.admin.domain.model.PasswordPolicy;

/**
 * 密码策略：委托 {@link PasswordPolicyConfigApi}，映射为领域 {@link PasswordPolicy}。
 */
@Component
@RequiredArgsConstructor
public class PasswordPolicyGatewayImpl implements PasswordPolicyGateway {

    private final PasswordPolicyConfigApi passwordPolicyConfigApi;

    @Override
    public PasswordPolicy current() {
        return toPasswordPolicy(passwordPolicyConfigApi.get());
    }

    private static PasswordPolicy toPasswordPolicy(PasswordPolicyConfig config) {
        if (config == null) {
            return new PasswordPolicy(
                PasswordPolicy.MIN_LENGTH_LOWER,
                PasswordPolicy.DEFAULT_MAX_LENGTH,
                false,
                false,
                PasswordPolicy.REPETITION_TIMES_LOWER,
                90
            );
        }
        return new PasswordPolicy(
            ObjectUtil.defaultIfNull(config.getPasswordMinLength(), PasswordPolicy.MIN_LENGTH_LOWER),
            PasswordPolicy.DEFAULT_MAX_LENGTH,
            Boolean.TRUE.equals(config.getPasswordRequireSpecial()),
            Boolean.TRUE.equals(config.getPasswordAllowContainUsername()),
            ObjectUtil.defaultIfNull(config.getPasswordRepetitionTimes(), PasswordPolicy.REPETITION_TIMES_LOWER),
            ObjectUtil.defaultIfNull(config.getPasswordExpireDays(), 90)
        );
    }
}
