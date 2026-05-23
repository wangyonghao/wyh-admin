
package top.wyhao.admin.system.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.constant.UiConstants;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 公告状态枚举
 *

 * @since 2023/8/20 10:55
 */
@Getter
@RequiredArgsConstructor
public enum NoticeStatus implements BaseEnum{

    /**
     * 草稿
     */
    DRAFT(1, "草稿", UiConstants.COLOR_WARNING),

    /**
     * 待发布
     */
    PENDING(2, "待发布", UiConstants.COLOR_PRIMARY),

    /**
     * 已发布
     */
    PUBLISHED(3, "已发布", UiConstants.COLOR_SUCCESS),;

    private final Integer value;
    private final String description;
    private final String color;
}
