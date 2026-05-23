
package top.wyhao.admin.schedule.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.constant.UiConstants;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 任务类型枚举

 * @since 2024/7/11 22:28
 */
@Getter
@RequiredArgsConstructor
public enum JobTaskTypeEnum {

    /**
     * 集群
     */
    CLUSTER(1, "集群", UiConstants.COLOR_PRIMARY),

    /**
     * 广播
     */
    BROADCAST(2, "广播", UiConstants.COLOR_PRIMARY),

    /**
     * 静态切片
     */
    SLICE(3, "静态切片", UiConstants.COLOR_PRIMARY),;

    private final Integer value;
    private final String description;
    private final String color;
}
