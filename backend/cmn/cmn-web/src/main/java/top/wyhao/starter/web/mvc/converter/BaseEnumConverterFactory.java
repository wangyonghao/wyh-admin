
package top.wyhao.starter.web.mvc.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import top.wyhao.starter.core.enums.BaseEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BaseEnum 参数转换器工厂
 *

 * @since 2.4.0
 */
public class BaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    private static final Map<Class, Converter> CONVERTER_CACHE = new ConcurrentHashMap<>();

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return CONVERTER_CACHE.computeIfAbsent(targetType, key -> new BaseEnumConverter<>(targetType));
    }
}
