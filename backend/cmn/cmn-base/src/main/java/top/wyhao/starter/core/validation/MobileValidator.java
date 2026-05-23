
package top.wyhao.starter.core.validation;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PhoneUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 手机号校验器
 *
 * <p>
 * 校验中国大陆手机号码
 * </p>
 *

 * @since 2.10.0
 */
public class MobileValidator implements ConstraintValidator<Mobile, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (CharSequenceUtil.isBlank(value)) {
            return true;
        }
        return PhoneUtil.isMobile(value);
    }
}
