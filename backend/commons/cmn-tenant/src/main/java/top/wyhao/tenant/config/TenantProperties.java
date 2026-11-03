
package top.wyhao.tenant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import top.wyhao.cmn.core.constant.PropertiesConstants;

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
}
