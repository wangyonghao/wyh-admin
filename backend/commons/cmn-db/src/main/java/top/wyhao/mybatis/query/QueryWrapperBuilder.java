package top.wyhao.mybatis.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据 DTO 上的 {@link Query} 注解反射组装 Wrapper。
 *
 * <p>返回的 Wrapper 可以继续链式追加手写条件，注解与 Lambda 两种写法可共存：
 * 通用条件交给注解，业务/权限条件用 Lambda 手写。</p>
 */
public final class QueryWrapperBuilder {

    private QueryWrapperBuilder() {
    }

    private static final Map<Class<?>, List<FieldMeta>> CACHE = new ConcurrentHashMap<>();

    /** 单表查询用这个，返回 LambdaQueryWrapper，后续可继续 .eq(User::getXxx, ...) */
    public static <T> LambdaQueryWrapper<T> build(Object dto, Class<T> entityClass) {
        return buildRaw(dto, entityClass).lambda();
    }

    /**
     * 关联查询用这个，返回字符串列名版 QueryWrapper，
     * 配合 Mapper XML 中的 ${ew.customSqlSegment} 使用（注解上记得写 alias）。
     */
    public static <T> QueryWrapper<T> buildRaw(Object dto, Class<T> entityClass) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if (dto == null) {
            return wrapper;
        }
        for (FieldMeta meta : CACHE.computeIfAbsent(dto.getClass(), QueryWrapperBuilder::parse)) {
            Object value = meta.read(dto);
            Query.Type type = meta.annotation.type();

            boolean nullCondition = type == Query.Type.IS_NULL || type == Query.Type.IS_NOT_NULL;
            if (nullCondition) {
                // IS NULL 类条件靠 Boolean.TRUE 开关控制
                if (!Boolean.TRUE.equals(value)) {
                    continue;
                }
            } else if (isEmpty(value)) {
                continue;
            }

            String[] properties = meta.properties;
            String alias = meta.annotation.alias();
            if (properties.length == 1) {
                apply(wrapper, ColumnResolver.resolve(entityClass, properties[0], alias), type, value);
            } else {
                // 多字段 OR：and( a like ? or b like ? )
                wrapper.and(outer -> {
                    for (String property : properties) {
                        String column = ColumnResolver.resolve(entityClass, property, alias);
                        outer.or(inner -> apply(inner, column, type, value));
                    }
                });
            }
        }
        return wrapper;
    }

    private static <T> void apply(QueryWrapper<T> w, String column, Query.Type type, Object value) {
        switch (type) {
            case EQ -> w.eq(column, value);
            case NE -> w.ne(column, value);
            case LIKE -> w.like(column, value);
            case LIKE_LEFT -> w.likeLeft(column, value);
            case LIKE_RIGHT -> w.likeRight(column, value);
            case GT -> w.gt(column, value);
            case GE -> w.ge(column, value);
            case LT -> w.lt(column, value);
            case LE -> w.le(column, value);
            case IN -> w.in(column, toCollection(value));
            case NOT_IN -> w.notIn(column, toCollection(value));
            case BETWEEN -> {
                List<?> range = new ArrayList<>(toCollection(value));
                if (range.size() != 2) {
                    throw new IllegalArgumentException("BETWEEN 需要长度为 2 的集合: " + column);
                }
                w.between(column, range.get(0), range.get(1));
            }
            case IS_NULL -> w.isNull(column);
            case IS_NOT_NULL -> w.isNotNull(column);
            default -> throw new IllegalArgumentException("不支持的查询类型: " + type);
        }
    }

    private static Collection<?> toCollection(Object value) {
        if (value instanceof Collection<?> c) {
            return c;
        }
        if (value instanceof Object[] arr) {
            return List.of(arr);
        }
        return List.of(value);
    }

    private static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence cs) {
            return cs.toString().isBlank();
        }
        if (value instanceof Collection<?> c) {
            return c.isEmpty();
        }
        if (value instanceof Object[] arr) {
            return arr.length == 0;
        }
        return false;
    }

    private static List<FieldMeta> parse(Class<?> dtoClass) {
        List<FieldMeta> list = new ArrayList<>();
        for (Class<?> c = dtoClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Query annotation = field.getAnnotation(Query.class);
                if (annotation == null) {
                    continue;
                }
                field.setAccessible(true);
                list.add(new FieldMeta(field, annotation));
            }
        }
        return list;
    }

    private record FieldMeta(Field field, Query annotation, String[] properties) {

        FieldMeta(Field field, Query annotation) {
            this(field, annotation, resolveProperties(field, annotation));
        }

        private static String[] resolveProperties(Field field, Query annotation) {
            String declared = annotation.field();
            if (declared == null || declared.isBlank()) {
                return new String[]{field.getName()};
            }
            return declared.split("\\s*,\\s*");
        }

        Object read(Object dto) {
            try {
                return field.get(dto);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("读取字段失败: " + field.getName(), e);
            }
        }
    }
}
