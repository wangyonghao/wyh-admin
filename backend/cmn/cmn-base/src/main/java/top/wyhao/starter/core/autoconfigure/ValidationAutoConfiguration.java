
package top.wyhao.starter.core.autoconfigure;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * JSR 303 校验器自动配置
 *

 * @since 2.3.0
 */
@AutoConfigureBefore
public class ValidationAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ValidationAutoConfiguration.class);

    /**
     * Validator 失败立即返回模式配置
     *
     * <p>
     * 默认情况下会校验完所有字段，然后才抛出异常。
     * </p>
     */
    @Bean
    public Validator validator(MessageSource messageSource) {
        try (LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean()) {
            // 国际化
            factoryBean.setValidationMessageSource(messageSource);
            // 快速失败
            factoryBean.getValidationPropertyMap().put(BaseHibernateValidatorConfiguration.FAIL_FAST, Boolean.TRUE.toString());
            factoryBean.afterPropertiesSet();
            return factoryBean.getValidator();
        }
    }

    @PostConstruct
    public void postConstruct() {
        log.debug("[wyhao-starter] - 'Validation' configured.");
    }
}
