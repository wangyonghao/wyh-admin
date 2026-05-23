
package top.wyhao.cmn.db.util;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import net.dreamlu.mica.core.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.exception.BadRequestException;
import top.wyhao.starter.core.util.ReflectUtils;
import top.wyhao.starter.core.util.validation.ValidationUtils;
import top.wyhao.cmn.db.query.Query;
import top.wyhao.cmn.db.query.QueryType;
import top.wyhao.cmn.db.query.LogicalRelation;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * QueryWrapper 工具类
 *

 */
public class QueryWrapperUtil {

    private static final Logger log = LoggerFactory.getLogger(QueryWrapperUtil.class);

    private QueryWrapperUtil() {
    }

    /**
     * 设置排序
     *
     * @param queryWrapper 查询条件封装对象
     * @param sort         排序条件
     * @since 2.9.0
     */
    public static <T> void applySort(QueryWrapper<T> queryWrapper, Sort sort, Class<T> entityClass) {
        if (sort == null || sort.isUnsorted()) {
            return;
        }
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String checkProperty;
            // 携带表别名则获取 . 后面的字段名
            if (property.contains(StringConstants.DOT)) {
                checkProperty = CollUtil.getLast(CharSequenceUtil.split(property, StringConstants.DOT));
            } else {
                checkProperty = property;
            }
            Optional<String> optional = getValidFieldNames(entityClass).stream()
                    .filter(checkProperty::equals)
                    .findFirst();
            ValidationUtils.throwIf(optional.isEmpty(), "无效的排序字段 [{}]", property);
            queryWrapper.orderBy(true, order.isAscending(), CharSequenceUtil.toUnderlineCase(property));
        }
    }

    /**
     * 反射获取实体类的所有属性名（含父类属性），转小写去重
     */
    private static Set<String> getValidFieldNames(Class<?> entityClass) {
        Set<String> fieldNames = new HashSet<>();
        // 遍历当前类及父类的所有字段
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            List<Field> fields = ReflectUtils.getNonStaticFields(currentClass);
            for (Field field : fields) {
                // 添加字段名（转小写，兼容前端传入大写/小写的情况）
                fieldNames.add(field.getName().toLowerCase());
            }
            currentClass = currentClass.getSuperclass();
        }
        return fieldNames;
    }

    /**
     * 构建 QueryWrapper
     *
     * @param query 查询条件
     * @param <Q>   查询条件数据类型
     * @param <R>   查询数据类型
     * @return QueryWrapper
     */
    public static <Q, R> QueryWrapper<R> build(Q query) {
        return build(query, Sort.unsorted());
    }

    /**
     * 构建 QueryWrapper
     *
     * @param query 查询条件
     * @param sort  排序条件
     * @param <Q>   查询条件数据类型
     * @param <R>   查询数据类型
     * @return QueryWrapper
     * @since 2.5.2
     */
    public static <Q, R> QueryWrapper<R> build(Q query, Sort sort) {
        QueryWrapper<R> queryWrapper = new QueryWrapper<>();
        // 没有查询条件，直接返回
        if (query == null) {
            return queryWrapper;
        }
        // 设置排序条件
        if (sort != null && sort.isSorted()) {
            for (Sort.Order order : sort) {
                String field = CharSequenceUtil.toUnderlineCase(order.getProperty());
                ValidationUtils.throwIf(SqlInjectionUtils.check(field), "排序字段包含无效字符");
                queryWrapper.orderBy(true, order.isAscending(), field);
            }
        }
        // 获取查询条件中所有的字段
        List<Field> fieldList = ReflectUtils.getNonStaticFields(query.getClass());
        return build(query, fieldList, queryWrapper);
    }

    /**
     * 构建 QueryWrapper
     *
     * @param query        查询条件
     * @param fields       查询条件字段列表
     * @param queryWrapper QueryWrapper
     * @param <Q>          查询条件数据类型
     * @param <R>          查询数据类型
     * @return QueryWrapper
     */
    public static <Q, R> QueryWrapper<R> build(Q query, List<Field> fields, QueryWrapper<R> queryWrapper) {
        // 没有查询条件，直接返回
        if (query == null) {
            return queryWrapper;
        }
        // 解析并拼接查询条件
        for (Field field : fields) {
            List<Consumer<QueryWrapper<R>>> consumers = buildWrapperConsumer(query, field);
            queryWrapper.and(CollUtil.isNotEmpty(consumers), q -> consumers.forEach(q::or));
        }
        return queryWrapper;
    }

    /**
     * 构建 QueryWrapper Consumer
     *
     * @param query 查询条件
     * @param field 查询条件字段
     * @param <Q>   查询条件数据类型
     * @param <R>   查询数据类型
     * @return QueryWrapper Consumer
     */
    private static <Q, R> List<Consumer<QueryWrapper<R>>> buildWrapperConsumer(Q query, Field field) {
        try {
            // 如果字段值为空，直接返回
            Object fieldValue = ReflectUtil.getFieldValue(query, field);
            if (ObjectUtil.isEmpty(fieldValue)) {
                return Collections.emptyList();
            }
            // 没有 @Query 注解，直接返回
            Query queryAnnotation = AnnotationUtil.getAnnotation(field, Query.class);
            if (queryAnnotation == null) {
                return Collections.emptyList();
            }
            // 建议：数据库表列建议采用下划线连接法命名，程序变量建议采用驼峰法命名
            String fieldName = ReflectUtil.getFieldName(field);
            // 解析单列查询
            QueryType queryType = queryAnnotation.type();
            String[] columns = queryAnnotation.columns();
            final int columnLength = ArrayUtil.length(columns);
            List<Consumer<QueryWrapper<R>>> consumers = new ArrayList<>(columnLength);
            if (columnLength <= 1) {
                String columnName = columnLength == 1 ? columns[0] : CharSequenceUtil.toUnderlineCase(fieldName);
                parse(queryType, columnName, fieldValue, consumers);
                return consumers;
            }
            // 解析多列查询
            LogicalRelation logicalRelation = queryAnnotation.logicalRelation();
            List<Consumer<QueryWrapper<R>>> columnConsumers = new ArrayList<>();
            for (String column : columns) {
                parse(queryType, column, fieldValue, columnConsumers);
            }

            if (logicalRelation == LogicalRelation.AND) {
                if (!columnConsumers.isEmpty()) {
                    consumers.add(q -> {
                        columnConsumers.get(0).accept(q);
                        columnConsumers.subList(1, columnConsumers.size()).forEach(q::and);
                    });
                }
            } else {
                consumers.addAll(columnConsumers);
            }
            return consumers;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Build query wrapper occurred an error: {}. Query: {}, Field: {}.", e
                .getMessage(), query, field, e);
        }
        return Collections.emptyList();
    }

    /**
     * 解析查询条件
     *
     * @param queryType  查询类型
     * @param columnName 列名
     * @param fieldValue 字段值
     * @param <R>        查询数据类型
     */
    private static <R> void parse(QueryType queryType,
                                  String columnName,
                                  Object fieldValue,
                                  List<Consumer<QueryWrapper<R>>> consumers) {
        switch (queryType) {
            case EQ -> consumers.add(q -> q.eq(columnName, fieldValue));
            case NE -> consumers.add(q -> q.ne(columnName, fieldValue));
            case GT -> consumers.add(q -> q.gt(columnName, fieldValue));
            case GE -> consumers.add(q -> q.ge(columnName, fieldValue));
            case LT -> consumers.add(q -> q.lt(columnName, fieldValue));
            case LE -> consumers.add(q -> q.le(columnName, fieldValue));
            case BETWEEN -> {
                // 数组转集合
                List<Object> between = new ArrayList<>(ArrayUtil.isArray(fieldValue)
                    ? List.of((Object[])fieldValue)
                    : (List<Object>)fieldValue);
                ValidationUtils.throwIf(between.size() != 2, "[{}] 必须是一个范围", columnName);
                consumers.add(q -> q.between(columnName, between.get(0), between.get(1)));
            }
            case LIKE -> consumers.add(q -> q.like(columnName, fieldValue));
            case LIKE_LEFT -> consumers.add(q -> q.likeLeft(columnName, fieldValue));
            case LIKE_RIGHT -> consumers.add(q -> q.likeRight(columnName, fieldValue));
            case IN -> {
                ValidationUtils.throwIfEmpty(fieldValue, "[{}] 不能为空", columnName);
                consumers.add(q -> q.in(columnName, ArrayUtil.isArray(fieldValue)
                    ? List.of((Object[])fieldValue)
                    : (Collection<Object>)fieldValue));
            }
            case NOT_IN -> {
                ValidationUtils.throwIfEmpty(fieldValue, "[{}] 不能为空", columnName);
                consumers.add(q -> q.notIn(columnName, ArrayUtil.isArray(fieldValue)
                    ? List.of((Object[])fieldValue)
                    : (Collection<Object>)fieldValue));
            }
            case IS_NULL -> consumers.add(q -> q.isNull(columnName));
            case IS_NOT_NULL -> consumers.add(q -> q.isNotNull(columnName));
            default -> throw new IllegalArgumentException("暂不支持 [%s] 查询类型".formatted(queryType));
        }
    }


    /**
     * 将 Spring Sort 应用到 LambdaQueryWrapper（自动字符串 → SFunction）
     */
    public static <T> void applySort(LambdaQueryWrapper<T> wrapper, Sort sort, Class<T> entityClass) {
        if (sort == null || sort.isUnsorted()) {
            return;
        }

        for (Sort.Order order : sort) {
            String fieldName = order.getProperty();
            boolean asc = order.getDirection().isAscending();

            try {
                // 核心：字符串 → SFunction
                SFunction<T, ?> sFunction = createSFunction(entityClass, fieldName);
                if (asc) {
                    wrapper.orderByAsc(sFunction);
                } else {
                    wrapper.orderByDesc(sFunction);
                }
            } catch (Throwable e) {
                throw new IllegalArgumentException("无效排序字段：" + fieldName, e);
            }
        }
    }

    private static final ConcurrentHashMap<String, SFunction<?, ?>> CACHE = new ConcurrentHashMap<>();

    /**
     * 字段名 createTime → SFunction(User::getCreateTime)
     */
    @SuppressWarnings("unchecked")
    public static <T> SFunction<T, ?> createSFunction(Class<T> entityClass, String fieldName) throws Throwable {
        String key = entityClass.getName() + "#" + fieldName;
        if (CACHE.containsKey(key)) {
            return (SFunction<T, ?>) CACHE.get(key);
        }

        // 获取字段
        Field field = entityClass.getDeclaredField(fieldName);

        // 获取 getter 方法
        String getterName = "get" + StringUtil.capitalize(field.getName());
        Method getter = entityClass.getMethod(getterName);

        // 动态生成 Lambda
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        var func = (SFunction<T, ?>) LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        MethodType.methodType(SFunction.class),
                        MethodType.methodType(Object.class, Object.class),
                        lookup.unreflect(getter),
                        MethodType.methodType(getter.getReturnType(), entityClass))
                .getTarget().invokeExact();

        CACHE.put(key, func);
        return func;
    }
}
