
package top.wyhao.starter.tenant.db;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wyhao.starter.tenant.config.TenantTableRegistry;
import top.wyhao.starter.tenant.context.TenantContextHolder;

/**
 * 默认租户行级隔离处理器
 *

 * @since 2.7.0
 */
@RequiredArgsConstructor
public class DefaultTenantLineHandler implements TenantLineHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultTenantLineHandler.class);

    private final TenantTableRegistry tenantTableRegistry;

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            return new LongValue(tenantId);
        }
        log.warn("Tenant ID not found in current context.");
        return new NullValue();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 忽略租户
        if (TenantContextHolder.isIgnore()) {
            return true;
        }
        // 忽略未继承 TenantedEntity 接口的表
        return !tenantTableRegistry.isTenantedTable(tableName);
    }
}