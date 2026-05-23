
package top.wyhao.starter.web.mvc.converter.time;

import cn.hutool.core.date.DateUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;

/**
 * LocalDate 参数转换器
 *

 * @since 2.10.0
 */
public class LocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate convert(@NonNull String source) {
        return DateUtil.parse(source).toLocalDateTime().toLocalDate();
    }
}
