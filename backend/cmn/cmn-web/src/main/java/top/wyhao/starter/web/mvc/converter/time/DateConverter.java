
package top.wyhao.starter.web.mvc.converter.time;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

/**
 * Date 参数转换器
 *

 * @since 2.10.0
 */
public class DateConverter implements Converter<String, Date> {

    /**
     * 将日期字符串转换为{@link DateTime}对象，格式：<br>
     * <ol>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy/MM/dd HH:mm:ss</li>
     * <li>yyyy.MM.dd HH:mm:ss</li>
     * <li>yyyy年MM月dd日 HH时mm分ss秒</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>HH:mm:ss</li>
     * <li>HH时mm分ss秒</li>
     * <li>yyyy-MM-dd HH:mm</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSSSSS</li>
     * <li>yyyyMMddHHmmss</li>
     * <li>yyyyMMddHHmmssSSS</li>
     * <li>yyyyMMdd</li>
     * <li>EEE, dd MMM yyyy HH:mm:ss z</li>
     * <li>EEE MMM dd HH:mm:ss zzz yyyy</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     * </ol>
     *
     * @param source 日期字符串
     * @return 日期对象
     */
    @Override
    public Date convert(@NonNull String source) {
        return DateUtil.parse(source);
    }
}
