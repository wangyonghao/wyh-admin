package top.wyhao.tenant.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.tenant.config.TenantProperties;
import top.wyhao.tenant.config.TenantProvider;
import top.wyhao.tenant.context.TenantContextHolder;
import top.wyhao.tenant.domain.TenantSelection;

/**
 * 租户拦截器
 */
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_ID = "X-Tenant-Id";

    private final TenantProvider tenantProvider;
    private final TenantProperties tenantProperties;

    public TenantInterceptor(TenantProvider tenantProvider, TenantProperties tenantProperties) {
        this.tenantProvider = tenantProvider;
        this.tenantProperties = tenantProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long tenantId = TenantSelection.select(sessionTenantId(), request.getHeader(TENANT_ID),
            tenantProperties.getDefaultTenantId());
        TenantContextHolder.setContext(tenantProvider.getByTenantId(tenantId.toString(), true));
        return true;
    }

    private static Long sessionTenantId() {
        if (!LoginUtil.isLogin()) {
            return null;
        }
        return LoginUtil.getTenantId();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        TenantContextHolder.clear();
    }
}
