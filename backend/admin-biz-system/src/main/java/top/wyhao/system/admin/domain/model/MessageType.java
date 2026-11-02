
package top.wyhao.system.admin.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.cmn.core.constant.UiConstants;
import top.wyhao.cmn.core.enums.BaseEnum;

/**
 * 消息类型枚举
 *

 * @since 2023/11/2 20:08
 */
@Getter
@RequiredArgsConstructor
public enum MessageType implements BaseEnum {

    /**
     * 系统消息
     */
    SYSTEM(1, "系统消息", UiConstants.COLOR_PRIMARY),

    /**
     * 安全消息
     */
    SECURITY(2, "安全消息", UiConstants.COLOR_WARNING),;

    private final Integer value;
    private final String description;
    private final String color;
}
