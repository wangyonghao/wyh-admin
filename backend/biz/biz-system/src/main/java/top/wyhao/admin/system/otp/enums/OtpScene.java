package top.wyhao.admin.system.otp.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OTP 业务场景枚举
 *

 */
@Getter
@RequiredArgsConstructor
public enum OtpScene {

    /**
     * 登录验证
     */
    LOGIN("登录验证"),

    /**
     * 注册验证
     */
    REGISTER("注册验证"),

    /**
     * 绑定邮箱
     */
    BIND_EMAIL("绑定邮箱"),

    /**
     * 绑定手机
     */
    BIND_PHONE("绑定手机"),

    /**
     * 重置密码
     */
    RESET_PASSWORD("重置密码"),

    /**
     * 修改邮箱
     */
    CHANGE_EMAIL("修改邮箱"),

    /**
     * 修改手机
     */
    CHANGE_PHONE("修改手机"),

    /**
     * 注销账号
     */
    DELETE_ACCOUNT("注销账号");

    private final String description;
}
