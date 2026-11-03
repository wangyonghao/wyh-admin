
package top.wyhao.cmn.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限枚举
 *

 * @since 2023/2/8 22:58
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum implements BaseEnum {

    /**
     * 全部数据权限
     */
    ALL(1, "全部数据权限"),

    /**
     * 本部门及以下数据权限
     */
    DEPARTMENT_AND_CHILD(2, "本部门及以下数据权限"),

    /**
     * 本部门数据权限
     */
    DEPARTMENT(3, "本部门数据权限"),

    /**
     * 仅本人数据权限
     */
    SELF(4, "仅本人数据权限"),

    /**
     * 自定义数据权限
     */
    CUSTOM_DEPARTMENT(5, "自定义部门数据权限"),;

    private final Integer value;
    private final String description;
}
