
package top.wyhao.admin.system.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 菜单类型枚举
 *

 * @since 2023/2/15 20:12
 */
@Getter
@RequiredArgsConstructor
public enum MenuType implements BaseEnum {

    /**
     * 目录
     */
    DIR(1, "目录"),

    /**
     * 菜单
     */
    MENU(2, "菜单"),

    /**
     * 按钮
     */
    BUTTON(3, "按钮"),;

    private final Integer value;
    private final String description;
}
