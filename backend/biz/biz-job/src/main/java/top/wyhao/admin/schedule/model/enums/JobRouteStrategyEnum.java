
package top.wyhao.admin.schedule.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 任务路由策略枚举

 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobRouteStrategyEnum{

    /**
     * 轮询
     */
    POLLING(4, "轮询"),

    /**
     * 随机
     */
    RANDOM(2, "随机"),

    /**
     * 一致性哈希
     */
    HASH(1, "一致性哈希"),

    /**
     * LRU
     */
    LRU(3, "LRU"),;

    private final Integer value;
    private final String description;
}
