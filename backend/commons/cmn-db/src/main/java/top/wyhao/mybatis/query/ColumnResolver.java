package top.wyhao.mybatis.query;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体属性名 -> 数据库列名 的解析器。
 *
 * <p>排序字段、动态查询字段都必须经过这里：解析不到就抛异常，
 * 等价于以实体定义为白名单，杜绝前端传 "1=1 union select ..." 之类的注入。</p>
 */
public final class ColumnResolver {

    private ColumnResolver() {
    }

    /** 缓存：实体类 -> (属性名 -> 列名) */
    private static final Map<Class<?>, Map<String, String>> CACHE = new ConcurrentHashMap<>();

    /**
     * @param entityClass 实体类（必须被 MyBatis-Plus 扫描过，即有对应 BaseMapper）
     * @param property    实体属性名，如 createTime
     * @return 数据库列名，如 create_time
     */
    public static String resolve(Class<?> entityClass, String property) {
        Map<String, String> map = CACHE.computeIfAbsent(entityClass, ColumnResolver::loadMapping);
        String column = map.get(property);
        if (column == null) {
            throw new IllegalArgumentException("非法字段: " + property
                    + "（不属于 " + entityClass.getSimpleName() + "）");
        }
        return column;
    }

    /** 带表别名，例如 resolve(User.class, "createTime", "u") -> u.create_time */
    public static String resolve(Class<?> entityClass, String property, String alias) {
        String column = resolve(entityClass, property);
        return (alias == null || alias.isEmpty()) ? column : alias + "." + column;
    }

    /** 主键属性名，无主键时返回 null */
    public static String keyProperty(Class<?> entityClass) {
        TableInfo info = TableInfoHelper.getTableInfo(entityClass);
        return (info != null && info.havePK()) ? info.getKeyProperty() : null;
    }

    /** 从方法引用取属性名，例如 User::getCreateTime -> createTime */
    public static <T> String propertyOf(SFunction<T, ?> fn) {
        try {
            Method writeReplace = fn.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) writeReplace.invoke(fn);
            return PropertyNamer.methodToProperty(lambda.getImplMethodName());
        } catch (Exception e) {
            throw new IllegalStateException("无法解析 Lambda 方法引用", e);
        }
    }

    private static Map<String, String> loadMapping(Class<?> entityClass) {
        TableInfo info = TableInfoHelper.getTableInfo(entityClass);
        if (info == null) {
            throw new IllegalStateException("未找到 TableInfo: " + entityClass.getName()
                    + "，请确认该实体有对应的 Mapper 且已被 @MapperScan 扫描");
        }
        Map<String, String> map = new ConcurrentHashMap<>();
        if (info.havePK()) {
            map.put(info.getKeyProperty(), info.getKeyColumn());
        }
        for (TableFieldInfo field : info.getFieldList()) {
            map.put(field.getProperty(), field.getColumn());
        }
        return map;
    }
}
