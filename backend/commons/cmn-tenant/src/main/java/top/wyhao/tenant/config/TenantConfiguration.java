package top.wyhao.tenant.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wyhao.tenant.annotation.ConditionalOnEnabledTenant;
import top.wyhao.tenant.spi.TenantStatusChecker;

/**
 * 租户配置
 *
 * <p>{@link TenantProperties} 仅由 {@code @EnableConfigurationProperties} 注册一份，
 * 避免与自动配置重复导致 {@code NoUniqueBeanDefinitionException}。
 */
@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class TenantConfiguration {

    /**
     * 租户提供者（需要业务侧提供 {@link TenantStatusChecker}）
     */
    @Bean
    @ConditionalOnEnabledTenant
    @ConditionalOnBean(TenantStatusChecker.class)
    public TenantProvider tenantProvider(TenantProperties tenantProperties,
                                         TenantStatusChecker tenantStatusChecker) {
        return new DefaultTenantProvider(tenantProperties, tenantStatusChecker);
    }

    /**
     * API 文档分组配置
     */
    @Bean
    public GroupedOpenApi tenantApi() {
        return GroupedOpenApi.builder().group("tenant").displayName("租户管理").pathsToMatch("/tenant/**").build();
    }
}
