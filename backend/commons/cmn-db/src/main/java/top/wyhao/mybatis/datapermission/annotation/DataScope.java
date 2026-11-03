
package top.wyhao.mybatis.datapermission.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解，用于 Mapper 类的方法之上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    String departmentAlias() default ""; // 部门ID字段所在表别名,如 "d"

    String userAlias() default ""; // 创建人字段所在表别名,如 "u"

    String departmentIdColumn() default "department_id";

    String userIdColumn() default "create_user";
}
