package top.wyhao.admin.system.otp.util;

import cn.hutool.core.util.StrUtil;

/**
 * 目标地址脱敏工具
 *
 * @author wyhao
 */
public class TargetMasker {

    /**
     * 脱敏邮箱地址
     * 示例：user@example.com -> u***@example.com
     *
     * @param email 邮箱地址
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 1) {
            return email;
        }

        String masked = username.charAt(0) + "***";
        return masked + "@" + domain;
    }

    /**
     * 脱敏手机号
     * 示例：13800138000 -> 138****8000
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }

        int length = phone.length();
        String prefix = phone.substring(0, 3);
        String suffix = phone.substring(length - 4);

        return prefix + "****" + suffix;
    }

    /**
     * 自动识别并脱敏
     *
     * @param target 目标地址
     * @return 脱敏后的地址
     */
    public static String mask(String target) {
        if (StrUtil.isBlank(target)) {
            return target;
        }

        if (target.contains("@")) {
            return maskEmail(target);
        } else {
            return maskPhone(target);
        }
    }
}
