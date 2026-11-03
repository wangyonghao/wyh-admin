package top.wyhao.tenant.core;

/**
 * 租户实体类接口

 */
public interface TenantedEntity {
    /**
     * 租户Id
     */
    Long getTenantId();
}
