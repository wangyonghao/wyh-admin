package top.wyhao.tenant.exception;

import cn.hutool.core.util.StrUtil;
import top.wyhao.cmn.core.exception.BizException;

/**
 * 租户业务异常
 */
public class TenantException extends BizException {

    public TenantException(String message) {
        super(message);
    }

    public TenantException(String code, String message) {
        super(code, message);
    }

    public static TenantException of(String message) {
        return new TenantException(message);
    }

    public static TenantException of(String code, String message) {
        return new TenantException(code, message);
    }

    public static TenantException nameExists(String name) {
        return of("TENANT_NAME_EXISTS", StrUtil.format("名称为 [{}] 的租户已存在", name));
    }

    public static TenantException domainExists(String domain) {
        return of("TENANT_DOMAIN_EXISTS", StrUtil.format("域名为 [{}] 的租户已存在", domain));
    }

    public static TenantException disabled() {
        return of("TENANT_DISABLED", "租户已被禁用");
    }

    public static TenantException expired() {
        return of("TENANT_EXPIRED", "租户已过期");
    }

    public static TenantException notFound() {
        return of("TENANT_NOT_FOUND", "租户不存在");
    }

    public static TenantException invalidId() {
        return of("TENANT_ID_INVALID", "租户 ID 不正确");
    }

    public static TenantException deleteNotAllowed() {
        return of("TENANT_DELETE_NOT_ALLOWED", "默认租户不允许删除");
    }
}
