package top.wyhao.system.admin.domain.service;

import org.springframework.stereotype.Component;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.model.PasswordPolicy;

/**
 * 管理员维护密码策略配置时的取值合法性规则。
 */
@Component
public class PasswordPolicyConfigRules {

    /**
     * 校验策略快照（设密相关字段）是否在合法范围内。
     */
    public void assertValid(PasswordPolicy policy) {
        assertInRange(policy.minLength(), PasswordPolicy.MIN_LENGTH_LOWER, PasswordPolicy.MIN_LENGTH_UPPER,
            "密码最小长度取值范围为 %d-%d");
        assertInRange(policy.maxLength(), policy.minLength(), PasswordPolicy.MIN_LENGTH_UPPER,
            "密码最大长度取值范围为 %d-%d");
        assertInRange(policy.historyRepetitionTimes(), PasswordPolicy.REPETITION_TIMES_LOWER,
            PasswordPolicy.REPETITION_TIMES_UPPER, "历史密码重复校验次数取值范围为 %d-%d");
        assertInRange(policy.expireDays(), PasswordPolicy.EXPIRE_DAYS_LOWER, PasswordPolicy.EXPIRE_DAYS_UPPER,
            "密码有效期取值范围为 %d-%d 天");
    }

    /**
     * 密码到期提醒天数须小于密码有效期（有效期 &gt; 0 时）。
     */
    public void assertExpirationWarning(int warningDays, int expireDays) {
        assertInRange(warningDays, PasswordPolicy.WARNING_DAYS_LOWER, PasswordPolicy.WARNING_DAYS_UPPER,
            "密码到期提醒取值范围为 %d-%d 天");
        if (expireDays > 0 && warningDays >= expireDays) {
            throw UserException.passwordWarningDaysExceedExpiration();
        }
    }

    /**
     * 密码错误锁定阈值与锁定时长。
     */
    public void assertErrorLock(int lockCount, int lockMinutes) {
        assertInRange(lockCount, PasswordPolicy.ERROR_LOCK_COUNT_LOWER, PasswordPolicy.ERROR_LOCK_COUNT_UPPER,
            "密码错误锁定阈值取值范围为 %d-%d");
        assertInRange(lockMinutes, PasswordPolicy.ERROR_LOCK_MINUTES_LOWER, PasswordPolicy.ERROR_LOCK_MINUTES_UPPER,
            "账号锁定时长取值范围为 %d-%d 分钟");
    }

    private void assertInRange(int value, int min, int max, String descriptionTemplate) {
        if (value < min || value > max) {
            throw UserException.passwordPolicyInvalid(descriptionTemplate.formatted(min, max));
        }
    }
}
