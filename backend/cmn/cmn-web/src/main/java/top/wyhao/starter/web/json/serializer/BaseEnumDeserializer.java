
package top.wyhao.starter.web.json.serializer;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import top.wyhao.starter.core.enums.BaseEnum;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 枚举接口 BaseEnum 反序列化器
 *
 * @see BaseEnum
 *

 * @since 2.4.0
 */
@JacksonStdImpl
public class BaseEnumDeserializer<T extends BaseEnum> extends JsonDeserializer<T> {

    /**
     * 静态实例
     */
    public static final BaseEnumDeserializer INSTANCE = new BaseEnumDeserializer();

    @Override
    public T deserialize(JsonParser jsonParser,
                                DeserializationContext deserializationContext) throws IOException {
        Class<?> targetClass = jsonParser.currentValue().getClass();
        String fieldName = jsonParser.currentName();
        String value = jsonParser.getText();
        return this.getEnum(targetClass, value, fieldName);
    }

    /**
     * 通过某字段对应值获取枚举实例，获取不到时为 {@code null}
     *
     * @param targetClass 目标类型
     * @param value       字段值
     * @param fieldName   字段名
     * @return 对应枚举实例 ，获取不到时为 {@code null}
     */
    private T getEnum(Class<?> targetClass, String value, String fieldName) {
        Field field = ReflectUtil.getField(targetClass, fieldName);
        Class<?> fieldTypeClass = field.getType();
        Object[] enumConstants = fieldTypeClass.getEnumConstants();
        for (Object enumConstant : enumConstants) {
            if (enumConstant instanceof BaseEnum) {
                T baseEnum = (T)enumConstant;
                if (Objects.equals(Convert.toStr(baseEnum.getValue()), Convert.toStr(value))) {
                    return baseEnum;
                }
            }
        }
        return null;
    }
}
