
package top.wyhao.starter.tenant.util;

import cn.hutool.extra.spring.SpringUtil;
import top.wyhao.starter.tenant.config.TenantProvider;
import top.wyhao.starter.tenant.context.TenantContext;
import top.wyhao.starter.tenant.context.TenantContextHolder;

/**
 * 租户工具类
 *


 * @since 2.13.1
 */
public class TenantUtils {

    private TenantUtils() {
    }

    /**
     * 使用指定租户执行业务逻辑
     *
     * <p>
     * 强制设置 {@code TenantContextHolder.setIgnore(false)}，执行完恢复原值。<br>
     * 适用于在非租户逻辑中执行有租户逻辑的业务逻辑。
     * </p>
     *
     * @param tenantId 租户 ID
     * @param runnable 业务逻辑
     */
    public static void execute(Long tenantId, Runnable runnable) {
        // 未启用租户，直接执行业务逻辑
        if (TenantContextHolder.isTenantDisabled()) {
            runnable.run();
            return;
        }
        // 原租户上下文
        TenantContext oldContext = TenantContextHolder.getContext();
        boolean oldIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContext newContext = SpringUtil.getBean(TenantProvider.class)
                .getByTenantId(tenantId.toString(), false);
            // 设置新租户上下文
            TenantContextHolder.setContext(newContext);
            TenantContextHolder.setIgnore(false);

            // 执行业务逻辑
            runnable.run();
        } finally {
            // 恢复原租户上下文
            TenantContextHolder.setContext(oldContext);
            TenantContextHolder.setIgnore(oldIgnore);
        }
    }

    /**
     * 忽略租户执行业务逻辑
     *
     * <p>
     * 适用于在租户逻辑中执行非租户逻辑的业务逻辑。
     * </p>
     *
     * @param runnable 业务逻辑
     */
    public static void executeIgnore(Runnable runnable) {
        // 未启用或忽略租户，直接执行业务逻辑
        if (TenantContextHolder.isTenantDisabled() || TenantContextHolder.isIgnore()) {
            runnable.run();
            return;
        }
        try {
            TenantContextHolder.setIgnore(true);
            // 执行业务逻辑
            runnable.run();
        } finally {
            TenantContextHolder.setIgnore(false);
        }
    }
}
