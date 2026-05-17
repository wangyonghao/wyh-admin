
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.entity.SysRoleMenu;
import top.wyhao.admin.system.mapper.RoleMenuMapper;
import top.wyhao.admin.system.service.RoleMenuService;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.cmn.db.model.ServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色和菜单业务实现
 *
 * @author Charles7c
 * @since 2023/2/19 10:43
 */
@Service
@RequiredArgsConstructor
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, SysRoleMenu> implements RoleMenuService {
    private final RoleMenuMapper roleMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(List<Long> menuIds, Long roleId) {
        // 检查是否有变更
        List<Long> oldMenuIdList = roleMenuMapper.lambdaQuery()
            .select(SysRoleMenu::getMenuId)
            .eq(SysRoleMenu::getRoleId, roleId)
            .list()
            .stream()
            .map(SysRoleMenu::getMenuId)
            .toList();
        if (CollUtil.isEmpty(CollUtil.disjunction(menuIds, oldMenuIdList))) {
            return false;
        }
        // 删除原有关联
        roleMenuMapper.lambdaUpdate().eq(SysRoleMenu::getRoleId, roleId).remove();
        // 保存最新关联
        List<SysRoleMenu> roleMenuList = CollUtils.mapToList(menuIds, menuId -> new SysRoleMenu(roleId, menuId));
        return roleMenuMapper.insertBatch(roleMenuList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRoleId(Long roleId) {
        roleMenuMapper.lambdaUpdate().in(SysRoleMenu::getRoleId, roleId).remove();
    }

    @Override
    public List<Long> listMenuIdByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return new ArrayList<>(0);
        }
        return roleMenuMapper.selectMenuIdByRoleIds(roleIds);
    }
}
