package top.wyhao.system.admin.domain.service;

import cn.hutool.core.text.CharSequenceUtil;
import top.wyhao.cmn.core.constant.RegexConstants;
import top.wyhao.system.admin.domain.exception.UserException;

import java.security.SecureRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 为邮箱注册分配内部用户名。
 *
 * <p>结果仍遵守现有 4–64 字符规则，不用邮箱本身。已有用户名不在这里改写。
 */
public final class RegisterUsernameAllocator {

    public static final int MAX_ATTEMPTS = 8;

    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int BODY_LENGTH = 11;

    private final Supplier<String> nextCandidate;

    public RegisterUsernameAllocator(Supplier<String> nextCandidate) {
        this.nextCandidate = nextCandidate;
    }

    public static RegisterUsernameAllocator secure() {
        SecureRandom random = new SecureRandom();
        return new RegisterUsernameAllocator(() -> {
            char[] body = new char[BODY_LENGTH];
            for (int i = 0; i < BODY_LENGTH; i++) {
                body[i] = ALPHABET[random.nextInt(ALPHABET.length)];
            }
            return "u" + new String(body);
        });
    }

    /**
     * @param passwordMustNotContainUsername 策略不允许密码包含用户名时，跳过会出现在密码里的候选
     */
    public String allocate(Predicate<String> taken, String rawPassword, boolean passwordMustNotContainUsername) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = nextCandidate.get();
            if (candidate == null || !candidate.matches(RegexConstants.USERNAME)) {
                continue;
            }
            if (taken.test(candidate)) {
                continue;
            }
            if (passwordMustNotContainUsername && containsName(rawPassword, candidate)) {
                continue;
            }
            return candidate;
        }
        throw UserException.of("REGISTER_USERNAME_UNAVAILABLE", "注册暂时无法完成，请稍后重试");
    }

    static boolean containsName(String rawPassword, String username) {
        if (rawPassword == null || username == null) {
            return false;
        }
        String reversed = new StringBuilder(username).reverse().toString();
        return CharSequenceUtil.containsAnyIgnoreCase(rawPassword, username, reversed);
    }
}
