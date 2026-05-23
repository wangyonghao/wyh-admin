
package top.wyhao.starter.core.spi;

import top.wyhao.starter.core.model.TenantBO;

/**
 * 租户数据 API
 * 


 * @since 2024/12/2 20:08
 */
public interface TenantDataApi {

    /**
     * 初始化数据
     *
     * @param tenant 租户信息
     */
    void init(TenantBO tenant);

    /**
     * 清除数据
     */
    void clear();
}
