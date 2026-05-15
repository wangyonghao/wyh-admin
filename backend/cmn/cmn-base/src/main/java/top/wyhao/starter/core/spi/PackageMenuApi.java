
package top.wyhao.starter.core.spi;

import java.util.List;

/**
 * 套餐和菜单关联业务 API
 *
 * @author Charles7c
 * @since 2025/7/23 21:13
 */
public interface PackageMenuApi {

    /**
     * 根据套餐 ID 查询
     *
     * @param packageId 套餐 ID
     * @return 菜单 ID 列表
     */
    List<Long> listMenuIdsByPackageId(Long packageId);
}
