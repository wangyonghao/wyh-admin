package top.wyhao.system.admin.domain.service;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.model.PasswordHistory;
import top.wyhao.system.admin.domain.model.PasswordPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 管理员保存密码策略时的取值边界。
 */
class PasswordPolicyConfigRulesTest {

    private final PasswordPolicyConfigRules rules = new PasswordPolicyConfigRules();

    @Test
    void acceptsBoundsOfLengthHistoryAndExpiration() {
        PasswordPolicy policy = policy(
                PasswordPolicy.MIN_LENGTH_LOWER,
                PasswordPolicy.MIN_LENGTH_UPPER,
                PasswordPolicy.REPETITION_TIMES_UPPER,
                PasswordPolicy.EXPIRE_DAYS_UPPER);

        assertThatCode(() -> rules.assertValid(policy)).doesNotThrowAnyException();
        assertThatCode(() -> rules.assertExpirationWarning(0, 0)).doesNotThrowAnyException();
        assertThatCode(() -> rules.assertExpirationWarning(PasswordPolicy.WARNING_DAYS_UPPER, 0))
                .doesNotThrowAnyException();
        assertThatCode(() -> rules.assertErrorLock(
                PasswordPolicy.ERROR_LOCK_COUNT_LOWER,
                PasswordPolicy.ERROR_LOCK_MINUTES_UPPER)).doesNotThrowAnyException();
    }

    @Test
    void rejectsLengthOutsideRangeOrMaxShorterThanMin() {
        assertInvalid(policy(7, 32, 3, 0), "密码最小长度");
        assertInvalid(policy(8, 33, 3, 0), "密码最大长度");
        assertInvalid(policy(16, 12, 3, 0), "密码最大长度");
    }

    @Test
    void rejectsHistoryAndExpirationOutsideRange() {
        assertInvalid(policy(8, 32, PasswordPolicy.REPETITION_TIMES_LOWER - 1, 0), "历史密码重复");
        assertInvalid(policy(8, 32, PasswordHistory.MAX_SIZE + 1, 0), "历史密码重复");
        assertInvalid(policy(8, 32, 3, PasswordPolicy.EXPIRE_DAYS_UPPER + 1), "密码有效期");
    }

    @Test
    void warningMustBeShorterThanAPositiveExpiration() {
        assertThatCode(() -> rules.assertExpirationWarning(7, 30)).doesNotThrowAnyException();

        assertThatThrownBy(() -> rules.assertExpirationWarning(30, 30))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode())
                        .isEqualTo("USER_PASSWORD_WARNING_DAYS_EXCEED_EXPIRATION"));
        assertThatThrownBy(() -> rules.assertExpirationWarning(PasswordPolicy.WARNING_DAYS_UPPER + 1, 0))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode())
                        .isEqualTo("USER_PASSWORD_POLICY_INVALID"));
    }

    @Test
    void rejectsLockThresholdAndDurationOutsideRange() {
        assertThatThrownBy(() -> rules.assertErrorLock(PasswordPolicy.ERROR_LOCK_COUNT_UPPER + 1, 30))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getDefaultMessage()).contains("锁定阈值"));
        assertThatThrownBy(() -> rules.assertErrorLock(5, 0))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getDefaultMessage()).contains("锁定时长"));
    }

    private static void assertInvalid(PasswordPolicy policy, String fragment) {
        assertThatThrownBy(() -> new PasswordPolicyConfigRules().assertValid(policy))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> {
                    UserException error = (UserException) ex;
                    assertThat(error.getCode()).isEqualTo("USER_PASSWORD_POLICY_INVALID");
                    assertThat(error.getDefaultMessage()).contains(fragment);
                });
    }

    private static PasswordPolicy policy(int minLength, int maxLength, int historyTimes, int expireDays) {
        return new PasswordPolicy(minLength, maxLength, false, true, historyTimes, expireDays);
    }
}
