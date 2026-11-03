package top.wyhao.system.admin.infrastructure.notify;

import cn.hutool.core.util.StrUtil;

/**
 * 目标地址脱敏工具
 */
public final class TargetMasker {

    private TargetMasker() {
    }

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
        return username.charAt(0) + "***@" + domain;
    }

    public static String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        int length = phone.length();
        return phone.substring(0, 3) + "****" + phone.substring(length - 4);
    }

    public static String mask(String target) {
        if (StrUtil.isBlank(target)) {
            return target;
        }
        return target.contains("@") ? maskEmail(target) : maskPhone(target);
    }
}
