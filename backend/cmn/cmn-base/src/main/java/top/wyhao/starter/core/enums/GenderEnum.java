
package top.wyhao.starter.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 性别枚举
 *

 * @since 2022/12/29 21:59
 */
@Getter
@RequiredArgsConstructor
public enum GenderEnum implements BaseEnum {

    /**
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 男
     */
    MALE(1, "男"),

    /**
     * 女
     */
    FEMALE(2, "女"),;

    private final Integer value;
    private final String description;


    public static GenderEnum getByValue(int value) {
        for (GenderEnum gender : GenderEnum.values()) {
            if (gender.getValue() == value) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
