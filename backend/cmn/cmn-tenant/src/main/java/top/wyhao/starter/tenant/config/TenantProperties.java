
package top.wyhao.starter.tenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wyhao.starter.core.constant.PropertiesConstants;

/**
 * 租户配置属性
 *

 * @since 2.7.0
 */
@ConfigurationProperties(PropertiesConstants.TENANT)
public class TenantProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 默认租户 ID（默认：0）
     */
    private Long defaultTenantId = 0L;
    /**
     * 请求头中租户 ID 键名
     */
    private String tenantIdHeader = "X-Tenant-Id";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getDefaultTenantId() {
        return defaultTenantId;
    }

    public void setDefaultTenantId(Long defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    public String getTenantIdHeader() {
        return tenantIdHeader;
    }

    public void setTenantIdHeader(String tenantIdHeader) {
        this.tenantIdHeader = tenantIdHeader;
    }
}
