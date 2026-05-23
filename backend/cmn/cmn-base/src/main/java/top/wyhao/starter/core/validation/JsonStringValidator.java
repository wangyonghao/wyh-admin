
package top.wyhao.starter.core.validation;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * JSON 格式字符串校验器
 *

 * @since 2.12.0
 */
public class JsonStringValidator implements ConstraintValidator<JsonString, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (CharSequenceUtil.isBlank(value)) {
            return true;
        }
        return JSONUtil.isTypeJSON(value);
    }
}