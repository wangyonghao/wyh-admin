
package top.wyhao.tenant.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.wyhao.cmn.core.constant.OrderedConstants;
import top.wyhao.cmn.core.constant.PropertiesConstants;
import top.wyhao.tenant.servlet.TenantInterceptor;

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

    private final TenantProvider tenantProvider;
    private final TenantProperties tenantProperties;

    public TenantWebMvcAutoConfiguration(TenantProvider tenantProvider, TenantProperties tenantProperties) {
        this.tenantProvider = tenantProvider;
        this.tenantProperties = tenantProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor(tenantProvider, tenantProperties))
            .order(OrderedConstants.Interceptor.TENANT_INTERCEPTOR);
    }
}
