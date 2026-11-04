
package top.wyhao.system.admin.app.service.impl;

import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageMenuDO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.admin.domain.PackageMenuBinding;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMenuMapper;
import top.wyhao.system.admin.domain.model.TenantPackageMenu;
import top.wyhao.system.admin.app.service.PackageMenuService;
import top.wyhao.cmn.core.util.CollUtils;

import java.util.List;

/**
 * 套餐和菜单关联业务实现
 *

 * @since 2025/7/13 20:45
 */
@Service
@RequiredArgsConstructor
public class PackageMenuServiceImpl implements PackageMenuService {

    private final TenantPackageMenuMapper baseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(List<Long> menuIds, Long packageId) {
        // 检查是否有变更
        List<Long> oldMenuIdList = baseMapper.lambdaQuery()
            .select(TenantPackageMenuDO::getMenuId)
            .eq(TenantPackageMenuDO::getPackageId, packageId)
            .list()
            .stream()
            .map(TenantPackageMenuDO::getMenuId)
            .toList();
        if (!PackageMenuBinding.changed(oldMenuIdList, menuIds)) {
            return false;
        }
        // 删除原有关联
        baseMapper.lambdaUpdate().eq(TenantPackageMenuDO::getPackageId, packageId).remove();
        List<Long> granted = PackageMenuBinding.menusForTenantAdmin(menuIds);
        if (granted.isEmpty()) {
            return true;
        }
        // 保存最新关联
        List<TenantPackageMenu> newList = CollUtils.mapToList(granted, menuId -> new TenantPackageMenu(packageId, menuId));
        return baseMapper.insertBatch(AdminDoMapper.toDoList(newList, TenantPackageMenuDO.class));
    }

    @Override
    public List<Long> listMenuIdsByPackageId(Long packageId) {
        return baseMapper.lambdaQuery()
            .select(TenantPackageMenuDO::getMenuId)
            .eq(TenantPackageMenuDO::getPackageId, packageId)
            .list()
            .stream()
            .map(TenantPackageMenuDO::getMenuId)
            .toList();
    }
}
