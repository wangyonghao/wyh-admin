
package top.wyhao.admin.schedule.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 任务阻塞策略枚举

 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobBlockStrategyEnum {

    /**
     * 丢弃
     */
    DISCARD(1, "丢弃"),

    /**
     * 覆盖
     */
    COVER(2, "覆盖"),

    /**
     * 并行
     */
    PARALLEL(3, "并行"),;

    private final Integer value;
    private final String description;
}
