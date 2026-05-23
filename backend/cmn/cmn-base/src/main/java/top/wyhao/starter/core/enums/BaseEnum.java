
package top.wyhao.starter.core.enums;

import java.util.Objects;

/**
 * 枚举接口
 *

 */
public interface BaseEnum {

    /**
     * 枚举值
     *
     * @return 枚举值
     */
    Integer getValue();

    String getDescription();
    /**
     * 根据枚举值获取
     *
     * @param value 枚举值
     * @return 枚举对象
     */
    static <E extends BaseEnum> E getByValue(Integer value, Class<E> clazz) {
        for (E e : clazz.getEnumConstants()) {
            if (Objects.equals(e.getValue(), value)) {
                return e;
            }
        }
        return null;
    }

}
