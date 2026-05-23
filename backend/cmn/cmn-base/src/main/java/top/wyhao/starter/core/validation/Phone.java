
package top.wyhao.starter.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 手机号校验注解
 *
 * <p>
 * 校验座机号码、手机号码（中国大陆）、手机号码（中国香港）、手机号码（中国台湾）、手机号码（中国澳门）
 * {@code @Phone(message = "手机号格式不正确")} <br />
 * </p>
 *

 * @since 2.13.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {

    /**
     * 提示消息
     *
     * @return 提示消息
     */
    String message() default "手机号格式不正确";

    /**
     * 分组
     *
     * @return 分组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     *
     * @return 负载
     */
    Class<? extends Payload>[] payload() default {};
}
