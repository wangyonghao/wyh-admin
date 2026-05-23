
package top.wyhao.starter.core.validation;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PhoneUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 手机号校验器
 *
 * <p>
 * 校验座机号码、手机号码（中国大陆）、手机号码（中国香港）、手机号码（中国台湾）、手机号码（中国澳门）
 * </p>
 *

 * @since 2.13.0
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (CharSequenceUtil.isBlank(value)) {
            return true;
        }
        return PhoneUtil.isPhone(value);
    }
}
