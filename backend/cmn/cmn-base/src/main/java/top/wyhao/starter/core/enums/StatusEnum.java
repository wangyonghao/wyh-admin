
package top.wyhao.starter.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用/禁用状态枚举
 *

 * @since 2022/12/29 22:38
 */
@Getter
@AllArgsConstructor
public enum StatusEnum implements BaseEnum {

    /**
     * 启用
     */
    ENABLE(1, "启用"),

    /**
     * 禁用
     */
    DISABLE(2, "禁用"),;

    private final Integer value;
    private final String description;
}
