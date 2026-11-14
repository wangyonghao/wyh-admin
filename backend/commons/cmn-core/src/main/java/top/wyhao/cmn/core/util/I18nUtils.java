package top.wyhao.cmn.core.util;

import cn.hutool.extra.spring.SpringUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 获取国际化资源文件
 *

 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class I18nUtils {

    /**
     * 根据消息键和参数获取消息，委托给 Spring MessageSource。
     * 延迟查找 Bean，避免类加载阶段在非 Spring 环境中失败。
     *
     * @param code 消息键
     * @param args 参数
     * @return 国际化翻译值；找不到或环境不可用时返回原 code
     */
    public static String message(String code, Object... args) {
        try {
            MessageSource messageSource = SpringUtil.getBean(MessageSource.class);
            if (messageSource == null) {
                return code;
            }
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return code;
        } catch (RuntimeException e) {
            return code;
        }
    }
}
