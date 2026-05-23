
package top.wyhao.admin.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.constant.UiConstants;

/**
 * 认证类型枚举
 *


 * @since 2024/12/22 14:52
 */
@Getter
@RequiredArgsConstructor
public enum AuthType {

    /**
     * 账号
     */
    ACCOUNT("ACCOUNT", "账号", UiConstants.COLOR_SUCCESS),

    /**
     * 邮箱
     */
    EMAIL("EMAIL", "邮箱", UiConstants.COLOR_PRIMARY),

    /**
     * 手机号
     */
    PHONE("PHONE", "手机号", UiConstants.COLOR_PRIMARY),

    /**
     * 第三方账号
     */
    SOCIAL("SOCIAL", "第三方账号", UiConstants.COLOR_ERROR);

    private final String value;
    private final String description;
    private final String color;
}
