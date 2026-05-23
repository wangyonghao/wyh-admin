
package top.wyhao.admin.tenant.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wyhao.admin.tenant.service.TenantService;
import top.wyhao.starter.tenant.annotation.ConditionalOnEnabledTenant;
import top.wyhao.starter.tenant.config.TenantProperties;
import top.wyhao.starter.tenant.config.TenantProvider;

/**
 * 租户配置
 *

 * @since 2025/7/12 13:30
 */
@Configuration
public class TenantConfiguration {

    /**
     * 租户扩展配置属性
     */
    @Bean
    public TenantProperties tenantProperties() {
        return new TenantProperties();
    }

    /**
     * 租户提供者
     */
    @Bean
    @ConditionalOnEnabledTenant
    public TenantProvider tenantProvider(TenantProperties tenantProperties,
                                         TenantService tenantService) {
        return new DefaultTenantProvider(tenantProperties, tenantService);
    }

    /**
     * API 文档分组配置
     */
    @Bean
    public GroupedOpenApi tenantApi() {
        return GroupedOpenApi.builder().group("tenant").displayName("租户管理").pathsToMatch("/tenant/**").build();
    }
}
