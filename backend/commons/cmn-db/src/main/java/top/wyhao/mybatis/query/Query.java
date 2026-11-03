
package top.wyhao.mybatis.query;

import java.lang.annotation.*;

/**
 * 标注在查询 DTO 的字段上，由 {@link QueryWrapperBuilder} 自动组装成 Wrapper 条件。
 * 字段值为 null / 空字符串 / 空集合时自动跳过，因此不需要写任何 if。
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface   Query {

    /**
     * 对应的实体属性名，默认与 DTO 字段同名。
     * 支持逗号分隔多个属性，此时会组装成一组 OR 条件（常用于关键字搜索）。
     * 例：@Query(type = LIKE, field = "username,displayName,phone")
     */
    String field() default "";

    /**
     * 条件类型（等值查询、模糊查询、范围查询等）
     */
    Type type() default Type.EQ;

    /** 表别名，关联查询时使用，如 "u" -> u.create_time */
    String alias() default "";

    enum Type {
        EQ, NE, LIKE, LIKE_LEFT, LIKE_RIGHT,
        GT, GE, LT, LE,
        IN, NOT_IN, BETWEEN,
        IS_NULL, IS_NOT_NULL
    }
}
