package top.wyhao.identity.client;

import top.wyhao.cmn.core.constant.RegexConstants;

import java.util.regex.Pattern;

/**
 * 注册实际执行的密码规则。
 *
 * <p>长度和特殊字符来自当前策略。小写字母和数字来自 {@link RegexConstants#PASSWORD_TEMPLATE}，
 * 配置里的大写/小写/数字开关不单独改变这条模板。
 */
public record EffectivePasswordRules(
        int minLength,
        int maxLength,
        boolean requireLowercase,
        boolean requireDigit,
        boolean requireSpecial
) {

    public static final int DEFAULT_MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 32;

    public static EffectivePasswordRules from(PasswordPolicyConfig config) {
        int min = DEFAULT_MIN_LENGTH;
        boolean special = false;
        if (config != null) {
            if (config.getPasswordMinLength() != null) {
                min = config.getPasswordMinLength();
            }
            special = Boolean.TRUE.equals(config.getPasswordRequireSpecial());
        }
        return new EffectivePasswordRules(min, MAX_LENGTH, true, true, special);
    }

    public boolean matches(String password) {
        if (password == null) {
            return false;
        }
        String lengthPattern = RegexConstants.PASSWORD_TEMPLATE.formatted(minLength, maxLength);
        if (!Pattern.compile(lengthPattern).matcher(password).matches()) {
            return false;
        }
        return !requireSpecial || Pattern.compile(RegexConstants.SPECIAL_CHARACTER).matcher(password).find();
    }
}
