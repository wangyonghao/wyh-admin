
package top.wyhao.starter.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 成功/失败状态枚举
 *

 * @since 2023/2/26 21:35
 */
@Getter
@AllArgsConstructor
public enum ResultStatusEnum implements BaseEnum {

    /**
     * 成功
     */
    SUCCESS(1, "成功"),

    /**
     * 失败
     */
    FAILURE(2, "失败"),;

    private final Integer value;
    private final String description;
}
