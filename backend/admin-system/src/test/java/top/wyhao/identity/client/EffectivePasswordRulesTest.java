package top.wyhao.identity.client;

import org.junit.jupiter.api.Test;
import top.wyhao.cmn.core.constant.RegexConstants;

import static org.assertj.core.api.Assertions.assertThat;

class EffectivePasswordRulesTest {

    @Test
    void defaultsMatchThePasswordTemplate() {
        EffectivePasswordRules rules = EffectivePasswordRules.from(null);
        assertThat(rules.minLength()).isEqualTo(8);
        assertThat(rules.maxLength()).isEqualTo(32);
        assertThat(rules.requireLowercase()).isTrue();
        assertThat(rules.requireDigit()).isTrue();
        assertThat(rules.requireSpecial()).isFalse();
        assertThat(rules.matches("abc12345")).isTrue();
        assertThat(rules.matches("abc1234")).isFalse();
        assertThat(rules.matches("ABCDEFG1")).isFalse();
        assertThat(rules.matches("abcdefgh")).isFalse();
        assertThat(rules.matches("a".repeat(33) + "1")).isFalse();
    }

    @Test
    void usesConfiguredMinimumAndSpecialFlag() {
        PasswordPolicyConfig config = new PasswordPolicyConfig();
        config.setPasswordMinLength(10);
        config.setPasswordRequireSpecial(true);
        config.setPasswordRequireUppercase(true);
        EffectivePasswordRules rules = EffectivePasswordRules.from(config);

        assertThat(rules.minLength()).isEqualTo(10);
        assertThat(rules.requireSpecial()).isTrue();
        assertThat(rules.matches("abc1234567")).isFalse();
        assertThat(rules.matches("abc123456!")).isTrue();
    }

    @Test
    void staysInsideTheLoginFormatGate() {
        PasswordPolicyConfig below = new PasswordPolicyConfig();
        below.setPasswordMinLength(6);
        EffectivePasswordRules low = EffectivePasswordRules.from(below);
        assertThat(low.minLength()).isEqualTo(8);
        assertThat(low.maxLength()).isEqualTo(32);
        assertThat(low.matches("abc123")).isFalse();
        assertThat(low.matches("abc12345")).isTrue();
        assertThat("abc12345".matches(RegexConstants.PASSWORD)).isTrue();

        PasswordPolicyConfig above = new PasswordPolicyConfig();
        above.setPasswordMinLength(40);
        EffectivePasswordRules high = EffectivePasswordRules.from(above);
        assertThat(high.minLength()).isEqualTo(32);
        String thirtyTwo = "a".repeat(31) + "1";
        assertThat(high.matches(thirtyTwo)).isTrue();
        assertThat(thirtyTwo.matches(RegexConstants.PASSWORD)).isTrue();
        assertThat(high.matches(thirtyTwo + "b")).isFalse();
    }
}
