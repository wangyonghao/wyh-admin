
package top.wyhao.admin.system.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 公告通知方式枚举
 *

 * @since 2025/5/8 21:18
 */
@Getter
@RequiredArgsConstructor
public enum NoticeMethods implements BaseEnum {

    /**
     * 系统消息
     */
    SYSTEM_MESSAGE(1, "系统消息"),

    /**
     * 登录弹窗
     */
    POPUP(2, "登录弹窗"),;

    private final Integer value;
    private final String description;
}
