package top.wyhao.system.admin.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.system.admin.domain.gateway.MenuRepository;
import top.wyhao.system.admin.domain.model.Menu;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMenuDO;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysMenuMapper;

import java.util.List;

/**
 * 菜单仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository {

    private final SysMenuMapper menuMapper;

    @Override
    public List<Menu> listAll() {
        return AdminDoMapper.toDomainList(menuMapper.selectList(new LambdaQueryWrapper<SysMenuDO>()
                .orderByAsc(SysMenuDO::getParentId)
                .orderByAsc(SysMenuDO::getSort)), Menu.class);
    }

    @Override
    public List<Menu> listEnabledTree() {
        return AdminDoMapper.toDomainList(menuMapper.selectList(new LambdaQueryWrapper<SysMenuDO>()
                .eq(SysMenuDO::getStatus, StatusEnum.ENABLE.getValue())
                .orderByAsc(SysMenuDO::getParentId)
                .orderByAsc(SysMenuDO::getSort)), Menu.class);
    }

    @Override
    public List<Menu> listEnabledCatalogAndMenu() {
        return AdminDoMapper.toDomainList(menuMapper.selectList(new LambdaQueryWrapper<SysMenuDO>()
                .eq(SysMenuDO::getStatus, 1)
                .in(SysMenuDO::getType, 1, 2)
                .orderByAsc(SysMenuDO::getParentId)
                .orderByAsc(SysMenuDO::getSort)), Menu.class);
    }

    @Override
    public List<Menu> listEnabled() {
        return AdminDoMapper.toDomainList(
                menuMapper.lambdaQuery().eq(SysMenuDO::getStatus, "1").list(),
                Menu.class);
    }

    @Override
    public List<Menu> listByUserId(Long userId) {
        return AdminDoMapper.toDomainList(menuMapper.selectMenusByUserId(userId), Menu.class);
    }

    @Override
    public List<Menu> listByRoleId(Long roleId) {
        return AdminDoMapper.toDomainList(menuMapper.selectListByRoleId(roleId), Menu.class);
    }

    @Override
    public Menu findById(Long id) {
        return AdminDoMapper.toDomain(menuMapper.selectById(id), Menu.class);
    }

    @Override
    public void insert(Menu menu) {
        SysMenuDO data = AdminDoMapper.toDo(menu, SysMenuDO.class);
        menuMapper.insert(data);
        AdminDoMapper.copyBack(data, menu);
    }

    @Override
    public void updateById(Menu menu) {
        SysMenuDO data = AdminDoMapper.toDo(menu, SysMenuDO.class);
        menuMapper.updateById(data);
        AdminDoMapper.copyBack(data, menu);
    }

    @Override
    public void updatePlacement(Long id, Long parentId, Integer sort) {
        menuMapper.lambdaUpdate()
                .eq(SysMenuDO::getId, id)
                .set(SysMenuDO::getParentId, parentId)
                .set(SysMenuDO::getSort, sort)
                .update();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        menuMapper.deleteByIds(ids);
    }

    @Override
    public boolean nameExists(String name, Long parentId, Long selfId) {
        return menuMapper.isNameExists(name, parentId, selfId);
    }

    @Override
    public List<Long> listChildIds(List<Long> parentIds) {
        return menuMapper.lambdaQuery()
                .select(SysMenuDO::getId)
                .in(SysMenuDO::getParentId, parentIds)
                .list()
                .stream()
                .map(SysMenuDO::getId)
                .toList();
    }

    @Override
    public List<String> listPermissionCodesByUserId(Long userId) {
        return menuMapper.selectPermissionByUserId(userId);
    }

    @Override
    public long countIdGreaterThan(Long id) {
        return menuMapper.lambdaQuery().gt(SysMenuDO::getId, id).count();
    }

    @Override
    public boolean deleteIdGreaterThan(Long id) {
        return menuMapper.lambdaUpdate().gt(SysMenuDO::getId, id).remove();
    }
}
