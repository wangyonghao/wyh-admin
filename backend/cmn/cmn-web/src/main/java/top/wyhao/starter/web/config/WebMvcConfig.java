
package top.wyhao.starter.web.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.wyhao.starter.web.norepeat.NoRepeatSubmitAspect;
import top.wyhao.starter.web.mvc.converter.BaseEnumConverterFactory;
import top.wyhao.starter.web.mvc.converter.time.DateConverter;
import top.wyhao.starter.web.mvc.converter.time.LocalDateConverter;
import top.wyhao.starter.web.mvc.converter.time.LocalDateTimeConverter;
import top.wyhao.starter.web.mvc.converter.time.LocalTimeConverter;

import java.util.List;
import java.util.Objects;

/**
 * Web MVC 自动配置
 *

 * @since 2.4.0
 */
@EnableWebMvc
@AutoConfiguration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);
    private final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    public WebMvcConfig(MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
        this.mappingJackson2HttpMessageConverter = mappingJackson2HttpMessageConverter;
    }

    /**
     * 解决 Jackson2ObjectMapperBuilderCustomizer 配置不生效的问题
     * <p>
     * MappingJackson2HttpMessageConverter 对象在程序启动时创建了多个，移除多余的，保证只有一个
     * </p>
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
        if (Objects.isNull(mappingJackson2HttpMessageConverter)) {
            converters.add(0, new MappingJackson2HttpMessageConverter());
        } else {
            converters.add(0, mappingJackson2HttpMessageConverter);
        }
        // 自定义 converters 时，需要手动在最前面添加 ByteArrayHttpMessageConverter
        // 否则 Spring Doc OpenAPI 的 /*/api-docs/**（例如：/v3/api-docs/default）接口响应内容会变为 Base64 编码后的内容，最终导致接口文档解析失败
        // 详情请参阅：https://github.com/springdoc/springdoc-openapi/issues/2143
        converters.add(0, new ByteArrayHttpMessageConverter());
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new BaseEnumConverterFactory());
        registry.addConverter(new DateConverter());
        registry.addConverter(new LocalDateTimeConverter());
        registry.addConverter(new LocalDateConverter());
        registry.addConverter(new LocalTimeConverter());
    }

    /**
     * 防重复请求切面
     */
    @Bean
    public NoRepeatSubmitAspect preventRepeatAspect() {
        return new NoRepeatSubmitAspect();
    }


    @PostConstruct
    public void postConstruct() {
        log.debug("[cmn-web] - 'WebMvc' configured.");
    }
}
