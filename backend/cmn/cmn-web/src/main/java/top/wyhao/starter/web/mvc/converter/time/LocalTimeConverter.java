
package top.wyhao.starter.web.mvc.converter.time;

import cn.hutool.core.date.DateUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalTime;

/**
 * LocalTime 参数转换器
 *

 * @since 2.10.0
 */
public class LocalTimeConverter implements Converter<String, LocalTime> {

    @Override
    public LocalTime convert(@NonNull String source) {
        return DateUtil.parse(source).toLocalDateTime().toLocalTime();
    }
}
