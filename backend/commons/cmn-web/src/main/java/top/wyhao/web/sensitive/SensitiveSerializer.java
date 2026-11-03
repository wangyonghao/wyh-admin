
package top.wyhao.web.sensitive;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import top.wyhao.cmn.core.constant.StringConstants;

import java.io.IOException;

/**
 * 敏感字段脱敏序列化器
 */
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private SensitiveMethod method;

    public SensitiveSerializer() {
        this.method = null;
    }

    public SensitiveSerializer(SensitiveMethod type) {
        this.method = type;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            Sensitive ann = property.getAnnotation(Sensitive.class);
            if (ann != null) {
                // 返回新实例保证线程安全
                return new SensitiveSerializer(ann.value());
            }
        }
        // 未标注注解，使用默认 String 序列化
        return provider.findValueSerializer(String.class, property);
    }


    @Override
    public void serialize(String str, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (CharSequenceUtil.isBlank(str)) {
            jsonGenerator.writeString(StringConstants.EMPTY);
            return;
        }
        // 使用自定义脱敏策略
        jsonGenerator.writeString(method.mask(str));
    }
}
