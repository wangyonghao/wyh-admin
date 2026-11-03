package top.wyhao.system.admin.domain.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHistoryTest {

    private static final PasswordEncoder IDENTITY = new PasswordEncoder() {
        @Override
        public String encode(CharSequence rawPassword) {
            return rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encodedPassword != null && encodedPassword.equals(rawPassword.toString());
        }
    };

    @Test
    void push_prependsAndCapsAtMax() {
        PasswordHistory history = PasswordHistory.empty();
        for (int i = 1; i <= 12; i++) {
            history = history.push("hash-" + i);
        }
        assertThat(history.hashes()).hasSize(10);
        assertThat(history.hashes().get(0)).isEqualTo("hash-12");
        assertThat(history.hashes().get(9)).isEqualTo("hash-3");
        assertThat(history.serialize()).startsWith("hash-12|hash-11|");
    }

    @Test
    void isReused_checksRecentOnly() {
        PasswordHistory history = PasswordHistory.parse("a|b|c");
        assertThat(history.isReused("b", IDENTITY, 2)).isTrue();
        assertThat(history.isReused("c", IDENTITY, 2)).isFalse();
        assertThat(history.isReused("c", IDENTITY, 3)).isTrue();
    }
}
