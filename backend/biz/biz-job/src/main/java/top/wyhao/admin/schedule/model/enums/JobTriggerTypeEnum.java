
package top.wyhao.admin.schedule.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 任务触发类型枚举

 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobTriggerTypeEnum {

    /**
     * 固定时间
     */
    FIXED_TIME(2, "固定时间"),

    /**
     * CRON
     */
    CRON(3, "CRON");

    private final Integer value;
    private final String description;
}
