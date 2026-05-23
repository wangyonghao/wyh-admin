
package top.wyhao.starter.web.excel;

import java.lang.annotation.*;

/**
 * 字典字段注解
 *

 * @since 2025/4/9 20:25
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DictExcelProperty {

    /**
     * 字典编码
     *
     * @return 字典编码
     */
    String value();
}
