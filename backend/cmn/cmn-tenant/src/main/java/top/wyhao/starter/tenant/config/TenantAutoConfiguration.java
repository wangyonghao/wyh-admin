
package top.wyhao.starter.tenant.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import top.wyhao.starter.core.constant.PropertiesConstants;
import top.wyhao.starter.tenant.db.DefaultTenantLineHandler;

/**
 * 租户自动配置
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = PropertiesConstants.TENANT, name = PropertiesConstants.ENABLED, havingValue = "true", matchIfMissing = true)
public class TenantAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TenantAutoConfiguration.class);

    public TenantAutoConfiguration() {
    }

    /**
     * 租户行级隔离拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantLineHandler tenantLineHandler) {
        return new TenantLineInnerInterceptor(tenantLineHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantTableRegistry tenantTableRegistry() {
        return new TenantTableRegistry();
    }


    /**
     * 租户行级隔离处理器（默认）
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantLineHandler tenantLineHandler(TenantTableRegistry tenantTableRegistry) {
        return new DefaultTenantLineHandler(tenantTableRegistry);
    }

    /**
     * 租户提供者
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantProvider tenantProvider() {
        if (log.isErrorEnabled()) {
            log.error("Consider defining a bean of type '{}' in your configuration.", ResolvableType
                .forClass(TenantProvider.class));
        }
        throw new NoSuchBeanDefinitionException(TenantProvider.class);
    }

    @PostConstruct
    public void postConstruct() {
        log.debug("[Tide Starter] - Auto Configuration 'Tenant' completed initialization.");
    }
}
