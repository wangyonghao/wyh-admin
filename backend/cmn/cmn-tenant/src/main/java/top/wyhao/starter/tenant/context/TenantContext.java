
package top.wyhao.starter.tenant.context;

import lombok.Getter;
import lombok.Setter;
import top.wyhao.starter.tenant.db.TenantIsolationLevel;

/**
 * 租户上下文
 *

 * @since 2.7.0
 */
@Setter
@Getter
public class TenantContext {

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 隔离级别
     */
    private TenantIsolationLevel isolationLevel = TenantIsolationLevel.LINE;

}
