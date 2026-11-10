package top.wyhao.system.admin.domain.service;

import org.junit.jupiter.api.Test;
import top.wyhao.cmn.core.constant.RegexConstants;
import top.wyhao.system.admin.domain.exception.UserException;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUsernameAllocatorTest {

    @Test
    void retriesTakenNamesAndSkipsNamesContainedInThePassword() {
        Queue<String> candidates = new ArrayDeque<>();
        candidates.add("takenname");
        candidates.add("secret99");
        candidates.add("freshname");
        Set<String> taken = new HashSet<>();
        taken.add("takenname");

        String allocated = new RegisterUsernameAllocator(candidates::remove)
                .allocate(taken::contains, "xxsecret99yy", true);

        assertThat(allocated).isEqualTo("freshname");
        assertThat(allocated).matches(RegexConstants.USERNAME);
        assertThat(allocated).doesNotContain("@");
    }

    @Test
    void stopsAfterTheAttemptLimit() {
        RegisterUsernameAllocator allocator = new RegisterUsernameAllocator(() -> "takenname");

        assertThatThrownBy(() -> allocator.allocate(name -> true, "password1", false))
                .isInstanceOf(UserException.class)
                .extracting(ex -> ((UserException) ex).getCode())
                .isEqualTo("REGISTER_USERNAME_UNAVAILABLE");
    }

    @Test
    void secureCandidatesStayInsideTheExistingUsernameRule() {
        RegisterUsernameAllocator allocator = RegisterUsernameAllocator.secure();
        String name = allocator.allocate(candidate -> false, "password1", true);
        assertThat(name).matches(RegexConstants.USERNAME);
        assertThat(name.length()).isBetween(4, 64);
        assertThat(name).doesNotContain("@");
    }
}
