
package top.wyhao.admin.tenant.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.tenant.mapper.TenantPackageMenuMapper;
import top.wyhao.admin.tenant.model.entity.TenantPackageMenu;
import top.wyhao.admin.tenant.service.PackageMenuService;
import top.wyhao.starter.core.util.CollUtils;

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
            .select(TenantPackageMenu::getMenuId)
            .eq(TenantPackageMenu::getPackageId, packageId)
            .list()
            .stream()
            .map(TenantPackageMenu::getMenuId)
            .toList();
        if (CollUtil.isEmpty(CollUtil.disjunction(menuIds, oldMenuIdList))) {
            return false;
        }
        // 删除原有关联
        baseMapper.lambdaUpdate().eq(TenantPackageMenu::getPackageId, packageId).remove();
        // 保存最新关联
        List<TenantPackageMenu> newList = CollUtils.mapToList(menuIds, menuId -> new TenantPackageMenu(packageId, menuId));
        return baseMapper.insertBatch(newList);
    }

    @Override
    public List<Long> listMenuIdsByPackageId(Long packageId) {
        return baseMapper.lambdaQuery()
            .select(TenantPackageMenu::getMenuId)
            .eq(TenantPackageMenu::getPackageId, packageId)
            .list()
            .stream()
            .map(TenantPackageMenu::getMenuId)
            .toList();
    }
}
