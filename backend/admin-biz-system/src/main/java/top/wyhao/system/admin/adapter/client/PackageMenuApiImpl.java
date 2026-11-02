
package top.wyhao.system.admin.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.system.admin.app.service.PackageMenuService;
import top.wyhao.tenant.client.PackageMenuApi;

import java.util.List;

/**
 * 套餐和菜单关联业务 API 实现
 *

 * @since 2025/7/23 21:13
 */
@Service
@RequiredArgsConstructor
public class PackageMenuApiImpl implements PackageMenuApi {

    private final PackageMenuService baseService;

    @Override
    public List<Long> listMenuIdsByPackageId(Long packageId) {
        return baseService.listMenuIdsByPackageId(packageId);
    }
}
