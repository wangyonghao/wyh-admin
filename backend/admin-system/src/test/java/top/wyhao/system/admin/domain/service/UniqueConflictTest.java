package top.wyhao.system.admin.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UniqueConflictTest {

    @Test
    void readsEmailAndUsernameConstraintNamesFromTheCauseChain() {
        RuntimeException email = new RuntimeException("insert failed",
                new IllegalStateException("duplicate key value violates unique constraint \"uk_user_email\""));
        RuntimeException username = new RuntimeException("duplicate key value violates unique constraint \"uk_user_username\"");

        assertThat(UniqueConflict.targetsEmail(email)).isTrue();
        assertThat(UniqueConflict.targetsUsername(email)).isFalse();
        assertThat(UniqueConflict.targetsUsername(username)).isTrue();
        assertThat(UniqueConflict.targetsEmail(username)).isFalse();
    }
}
