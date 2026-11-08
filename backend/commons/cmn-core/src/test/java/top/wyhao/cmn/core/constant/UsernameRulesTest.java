package top.wyhao.cmn.core.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 写入用户名长度与字符类。不连数据库。
 */
class UsernameRulesTest {

    @Test
    void lengthIsInclusiveFrom4To64() {
        assertThat(matches(sample(3))).isFalse();
        assertThat(matches(sample(4))).isTrue();
        assertThat(matches(sample(64))).isTrue();
        assertThat(matches(sample(65))).isFalse();
    }

    @Test
    void rejectsNullBlankAndEmpty() {
        assertThat(matches(null)).isFalse();
        assertThat(matches("")).isFalse();
        assertThat(matches("   ")).isFalse();
    }

    @Test
    void keepsLetterDigitUnderscoreClass() {
        assertThat(matches("ab_d")).isTrue();
        assertThat(matches("Abcd")).isTrue();
        assertThat(matches("1abc")).isFalse();
        assertThat(matches("ab-d")).isFalse();
        assertThat(RegexConstants.USERNAME).isEqualTo("^[a-zA-Z][a-zA-Z0-9_]{3,63}$");
    }

    private static boolean matches(String value) {
        return value != null && value.matches(RegexConstants.USERNAME);
    }

    private static String sample(int length) {
        return "a" + "b".repeat(length - 1);
    }
}
