package top.wyhao.system.admin.domain.model;

import cn.hutool.core.text.CharSequenceUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 曾用密码哈希列表（新在前），用于重复校验与落库序列化。
 *
 * <p>存储格式：多个哈希用 {@link #DELIMITER} 拼接；BCrypt 字符集不含该分隔符。
 */
public final class PasswordHistory {

    public static final int MAX_SIZE = 10;

    private static final char DELIMITER = '|';

    private final List<String> hashes;

    private PasswordHistory(List<String> hashes) {
        this.hashes = List.copyOf(hashes);
    }

    public static PasswordHistory empty() {
        return new PasswordHistory(List.of());
    }

    public static PasswordHistory parse(String stored) {
        if (CharSequenceUtil.isBlank(stored)) {
            return empty();
        }
        String[] parts = CharSequenceUtil.splitToArray(stored, DELIMITER);
        List<String> list = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (CharSequenceUtil.isNotBlank(part)) {
                list.add(part);
            }
        }
        return new PasswordHistory(list);
    }

    /**
     * 将旧密码哈希插入队头，并裁剪到 {@link #MAX_SIZE}。
     */
    public PasswordHistory push(String encodedPassword) {
        List<String> next = new ArrayList<>(hashes.size() + 1);
        if (CharSequenceUtil.isNotBlank(encodedPassword)) {
            next.add(encodedPassword);
        }
        next.addAll(hashes);
        if (next.size() > MAX_SIZE) {
            next = new ArrayList<>(next.subList(0, MAX_SIZE));
        }
        return new PasswordHistory(next);
    }

    /**
     * 明文是否与最近 {@code count} 条曾用哈希匹配。
     */
    public boolean isReused(String rawPassword, PasswordEncoder encoder, int count) {
        if (encoder == null || CharSequenceUtil.isBlank(rawPassword) || count <= 0 || hashes.isEmpty()) {
            return false;
        }
        int limit = Math.min(count, hashes.size());
        for (int i = 0; i < limit; i++) {
            if (encoder.matches(rawPassword, hashes.get(i))) {
                return true;
            }
        }
        return false;
    }

    public String serialize() {
        if (hashes.isEmpty()) {
            return null;
        }
        return String.join(String.valueOf(DELIMITER), hashes);
    }

    public List<String> hashes() {
        return Collections.unmodifiableList(hashes);
    }
}
