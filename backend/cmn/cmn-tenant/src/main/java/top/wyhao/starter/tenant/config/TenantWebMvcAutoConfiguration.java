
package top.wyhao.starter.tenant.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.wyhao.starter.core.constant.OrderedConstants;
import top.wyhao.starter.core.constant.PropertiesConstants;
import top.wyhao.starter.tenant.servlet.TenantInterceptor;

/**
 * 租户 Web MVC 自动配置
 *

 * @since 2.7.0
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(TenantProperties.class)
@ConditionalOnProperty(prefix = PropertiesConstants.TENANT, name = PropertiesConstants.ENABLED, havingValue = "true", matchIfMissing = true)
public class TenantWebMvcAutoConfiguration implements WebMvcConfigurer {

    private final TenantProperties tenantProperties;
    private final TenantProvider tenantProvider;

    public TenantWebMvcAutoConfiguration(TenantProperties tenantProperties, TenantProvider tenantProvider) {
        this.tenantProperties = tenantProperties;
        this.tenantProvider = tenantProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor(tenantProperties, tenantProvider))
            .order(OrderedConstants.Interceptor.TENANT_INTERCEPTOR);
    }
}
