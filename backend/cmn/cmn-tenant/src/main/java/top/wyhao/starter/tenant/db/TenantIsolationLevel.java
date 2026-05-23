
package top.wyhao.starter.tenant.db;

/**
 * 租户隔离级别
 *

 * @since 2.7.0
 */
public enum TenantIsolationLevel {

    /**
     * 行级
     */
    LINE,

    /**
     * 数据源级
     */
    DATASOURCE
}
