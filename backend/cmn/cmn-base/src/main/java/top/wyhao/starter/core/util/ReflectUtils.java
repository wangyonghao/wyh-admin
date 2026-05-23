
package top.wyhao.starter.core.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.exception.SystemException;

import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * 反射工具类
 *

 * @since 1.0.0
 */
public class ReflectUtils {

    private static final String SETTER_PREFIX = "set";
    private static final String GETTER_PREFIX = "get";

    private ReflectUtils() {
    }

    /**
     * 获得一个类中所有非静态字段名列表，包括其父类中的字段<br>
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass 类
     * @return 非静态字段名列表
     * @throws SecurityException 安全检查异常
     */
    public static List<String> getNonStaticFieldsName(Class<?> beanClass) throws SecurityException {
        List<Field> nonStaticFields = getNonStaticFields(beanClass);
        return CollUtils.mapToList(nonStaticFields, Field::getName);
    }

    /**
     * 获得一个类中所有非静态字段列表，包括其父类中的字段<br>
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass 类
     * @return 非静态字段列表
     * @throws SecurityException 安全检查异常
     */
    public static List<Field> getNonStaticFields(Class<?> beanClass) throws SecurityException {
        Field[] fields = ReflectUtil.getFields(beanClass);
        return Arrays.stream(fields).filter(f -> !Modifier.isStatic(f.getModifiers())).toList();
    }

    /**
     * 通过反射创建方法引用，支持在父类中查找方法
     *
     * @param clazz      实体类类型
     * @param methodName 方法名
     * @param <T>        实体类类型
     * @param <K>        返回值类型
     * @return Function<T, K> 方法引用
     * @throws IllegalArgumentException 如果参数不合法
    
     * @since 2.13.2
     */
    @SuppressWarnings("unchecked")
    public static <T, K> Function<T, K> createMethodReference(Class<T> clazz, String methodName) {
        try {
            Method method = ReflectUtil.getMethodByName(clazz, methodName);
            return MethodHandleProxies.asInterfaceInstance(Function.class, MethodHandles.lookup().unreflect(method));
        } catch (Exception e) {
            throw new SystemException("创建方法引用失败：" + clazz.getName() + StringConstants.DOT + methodName);
        }
    }

    /**
     * 调用Getter方法.
     * 支持多级，如：对象名.对象名.方法
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeGetter(Object obj, String propertyName) {
        Object object = obj;
        for (String name : CharSequenceUtil.split(propertyName, ".")) {
            String getterMethodName = GETTER_PREFIX + CharSequenceUtil.upperFirst(name);
            object = ReflectUtil.invoke(object, getterMethodName);
        }
        return (E) object;
    }

    /**
     * 调用Setter方法, 仅匹配方法名。
     * 支持多级，如：对象名.对象名.方法
     */
    public static <E> void invokeSetter(Object obj, String propertyName, E value) {
        Object object = obj;
        List<String> names = CharSequenceUtil.split(propertyName, ".");
        for (int i = 0; i < names.size(); i++) {
            if (i < names.size() - 1) {
                String getterMethodName = GETTER_PREFIX + CharSequenceUtil.upperFirst(names.get(i));
                object = ReflectUtil.invoke(object, getterMethodName);
            } else {
                String setterMethodName = SETTER_PREFIX + CharSequenceUtil.upperFirst(names.get(i));
                Method method = ReflectUtil.getMethodByName(object.getClass(), setterMethodName);
                ReflectUtil.invoke(object, method, value);
            }
        }
    }

}
