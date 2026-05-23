
package top.wyhao.admin.system.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 操作状态枚举
 *

 * @since 2022/12/25 9:09
 */
@Getter
@RequiredArgsConstructor
public enum LogStatus implements BaseEnum {

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
