package top.wyhao.identity.client;

import org.junit.jupiter.api.Test;

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
}
