package top.wyhao.admin.system.otp.util;

import java.security.SecureRandom;

/**
 * OTP 验证码生成器
 *
 * @author wyhao
 */
public class OtpCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成指定长度的数字验证码
     *
     * @param length 验证码长度
     * @return 验证码
     */
    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("验证码长度必须大于 0");
        }

        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(RANDOM.nextInt(10));
        }

        String result = code.toString();

        // 避免连续数字（如 123456）
        if (isSequential(result)) {
            return generate(length);
        }

        // 避免重复数字（如 111111）
        if (isRepeating(result)) {
            return generate(length);
        }

        return result;
    }

    /**
     * 判断是否为连续数字
     */
    private static boolean isSequential(String code) {
        if (code.length() < 3) {
            return false;
        }

        for (int i = 0; i < code.length() - 2; i++) {
            int a = code.charAt(i) - '0';
            int b = code.charAt(i + 1) - '0';
            int c = code.charAt(i + 2) - '0';

            if (b == a + 1 && c == b + 1) {
                return true;
            }
            if (b == a - 1 && c == b - 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否为重复数字
     */
    private static boolean isRepeating(String code) {
        if (code.length() < 2) {
            return false;
        }

        char first = code.charAt(0);
        for (int i = 1; i < code.length(); i++) {
            if (code.charAt(i) != first) {
                return false;
            }
        }

        return true;
    }
}
