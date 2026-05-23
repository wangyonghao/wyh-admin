
package top.wyhao.starter.apidoc.autoconfigure;

import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.wyhao.starter.apidoc.handler.BaseEnumParameterHandler;
import top.wyhao.starter.core.autoconfigure.application.ApplicationProperties;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.core.util.GeneralPropertySourceFactory;

import java.util.List;
import java.util.Map;

/**
 * API 文档自动配置
 *

 * @since 1.0.0
 */
@EnableWebMvc
@AutoConfiguration(before = SpringDocConfiguration.class)
@EnableConfigurationProperties(SpringDocExtensionProperties.class)
@PropertySource(value = "classpath:default-api-doc.yml", factory = GeneralPropertySourceFactory.class)
public class SpringDocAutoConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SpringDocAutoConfiguration.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/");
    }

    /**
     * Open API 配置
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openApi(ApplicationProperties applicationProperties, SpringDocExtensionProperties properties) {
        Info info = new Info().title("%s %s".formatted(applicationProperties.getName(), "API 文档"))
            .version(applicationProperties.getVersion())
            .description(applicationProperties.getDescription());
        ApplicationProperties.Contact contact = applicationProperties.getContact();
        if (contact != null) {
            info.contact(new Contact().name(contact.getName()).email(contact.getEmail()).url(contact.getUrl()));
        }
        ApplicationProperties.License license = applicationProperties.getLicense();
        if (license != null) {
            info.license(new License().name(license.getName()).url(license.getUrl()));
        }
        OpenAPI openApi = new OpenAPI();
        openApi.info(info);
        Components components = properties.getComponents();
        if (components != null) {
            openApi.components(components);
            // 鉴权配置
            Map<String, SecurityScheme> securitySchemeMap = components.getSecuritySchemes();
            if (MapUtil.isNotEmpty(securitySchemeMap)) {
                SecurityRequirement securityRequirement = new SecurityRequirement();
                List<String> list = CollUtils.mapToList(securitySchemeMap.values(), SecurityScheme::getName);
                list.forEach(securityRequirement::addList);
                openApi.addSecurityItem(securityRequirement);
            }
        }
        return openApi;
    }

    /**
     * 全局自定义配置（全局添加鉴权参数）
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer(SpringDocExtensionProperties properties) {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    // 为所有接口添加鉴权
                    Components components = properties.getComponents();
                    if (components != null && MapUtil.isNotEmpty(components.getSecuritySchemes())) {
                        Map<String, SecurityScheme> securitySchemeMap = components.getSecuritySchemes();
                        pathItem.readOperations().forEach(operation -> {
                            SecurityRequirement securityRequirement = new SecurityRequirement();
                            securitySchemeMap.keySet().forEach(securityRequirement::addList);
                            operation.addSecurityItem(securityRequirement);
                        });
                    }
                });
            }
        };
    }

    /**
     * 自定义 BaseEnum 枚举参数配置（针对实现了 BaseEnum 的枚举，优化其枚举值和描述展示）
     *
     * @return {@link BaseEnumParameterHandler }
     * @since 2.4.0
     */
    @Bean
    public BaseEnumParameterHandler customParameterCustomizer(ObjectMapper mapper) {
        return new BaseEnumParameterHandler(mapper);
    }

    @PostConstruct
    public void postConstruct() {
        log.debug("[Tide Starter] - Auto Configuration 'ApiDoc' completed initialization.");
    }
}
