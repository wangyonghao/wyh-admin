
package top.wyhao.admin.schedule.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.constant.UiConstants;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 任务状态枚举

 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobStatusEnum {

    /**
     * 禁用
     */
    DISABLED(0, "禁用", UiConstants.COLOR_ERROR),

    /**
     * 启用
     */
    ENABLED(1, "启用", UiConstants.COLOR_SUCCESS),;

    private final Integer value;
    private final String description;
    private final String color;
}
