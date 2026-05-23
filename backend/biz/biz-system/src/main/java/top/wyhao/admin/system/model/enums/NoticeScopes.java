
package top.wyhao.admin.system.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.wyhao.starter.core.enums.BaseEnum;

/**
 * 公告通知范围枚举
 *

 * @since 2023/8/20 10:55
 */
@Getter
@RequiredArgsConstructor
public enum NoticeScopes implements BaseEnum {

    /**
     * 所有人
     */
    ALL(1, "所有人"),

    /**
     * 指定用户
     */
    USER(2, "指定用户"),;

    private final Integer value;
    private final String description;
}
