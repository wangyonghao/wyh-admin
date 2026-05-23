
package top.wyhao.starter.web.mvc.converter.time;

import cn.hutool.core.date.DateUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;

/**
 * LocalDateTime 参数转换器
 *

 * @since 2.10.0
 */
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(@NonNull String source) {
        return DateUtil.parse(source).toLocalDateTime();
    }
}
