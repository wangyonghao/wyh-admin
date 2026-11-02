
package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.admin.app.assembler.MenuAssembler;
import top.wyhao.system.admin.domain.gateway.MenuRepository;
import top.wyhao.system.admin.domain.gateway.RoleMenuRepository;
import top.wyhao.system.admin.domain.gateway.UserRoleRepository;
import top.wyhao.system.admin.domain.model.Menu;
import top.wyhao.system.admin.domain.exception.MenuException;
import top.wyhao.cmn.core.constant.SystemConstants;
import top.wyhao.system.admin.domain.model.MenuType;
import top.wyhao.system.admin.app.dto.MenuTreeVO;
import top.wyhao.system.admin.app.dto.MenuVO;
import top.wyhao.system.admin.app.service.MenuService;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.cmn.core.constant.CacheConstants;
import top.wyhao.cmn.core.constant.StringConstants;
import top.wyhao.cmn.core.enums.RoleCodeEnum;
import top.wyhao.cmn.core.util.TreeUtils;
import top.wyhao.system.admin.app.dto.MenuQuery;
import top.wyhao.system.admin.app.dto.MenuRequest;
import top.wyhao.system.admin.app.dto.MenuSortRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 菜单 Service
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final UserRoleRepository userRoleRepository;
    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final MenuAssembler menuAssembler;

    @Override
    public List<MenuTreeVO> tree(MenuQuery query) {
        return buildPermissionTree(menuRepository.listEnabledTree());
    }

    @Override
    public List<MenuTreeVO> getMenuTreeByUserId(Long userId) {
        // 获取用户的角色ID列表
        List<String> roleCodes = userRoleRepository.listRoleCodesByUserId(userId);
        if (roleCodes.contains(RoleCodeEnum.SUPER_ADMIN.getCode())) {
            return this.buildPermissionTree(menuRepository.listEnabledCatalogAndMenu());
        }
        List<Menu> menus = menuRepository.listByUserId(userId);
        return this.buildPermissionTree(menus);
    }

    @Override
    public List<MenuVO> listByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        // 如果包含超级管理员角色，则返回所有启用的菜单
        if (roleIds.contains(SystemConstants.SUPER_ADMIN_ROLE_ID)) {
            return menuAssembler.toVOList(menuRepository.listEnabled());
        }

        List<Menu> allMenus = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<Menu> menusForRole = menuRepository.listByRoleId(roleId);
            allMenus.addAll(menusForRole);
        }

        // 去重并转换为响应对象
        return menuAssembler.toVOList(allMenus.stream().distinct().toList());
    }


    @Override
    public List<MenuVO> list(MenuQuery query) {
        return List.of();
    }

    @Override
    public void export(MenuQuery query, HttpServletResponse response) {

    }


    @Override
    public MenuVO get(Long id) {
        Menu sysMenu = menuRepository.findById(id);
        if (sysMenu == null) {
            throw MenuException.notFound();
        }
        return menuAssembler.toVO(sysMenu);
    }

    @Override
    public List<MenuVO> listAll() {
        return menuAssembler.toVOList(menuRepository.listAll());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sort(MenuSortRequest req) {
        Map<Long, Menu> byId = new HashMap<>();
        for (Menu menu : menuRepository.listAll()) {
            byId.put(menu.getId(), menu);
        }
        for (MenuSortRequest.Item item : req.getItems()) {
            Menu node = byId.get(item.getId());
            if (node == null) {
                throw MenuException.notFound();
            }
            long parentId = item.getParentId();
            assertParent(byId, node, parentId);
            if (!MenuType.BUTTON.equals(node.getType())
                    && parentOf(node) != parentId
                    && menuRepository.nameExists(node.getName(), parentId, node.getId())) {
                throw MenuException.titleExist(node.getName());
            }
        }
        for (MenuSortRequest.Item item : req.getItems()) {
            menuRepository.updatePlacement(item.getId(), item.getParentId(), item.getSort());
        }
        clearMenuCache();
    }

    @Override
    public Long create(MenuRequest req) {
        if (req.getParentId() == null) {
            req.setParentId(0L);
        }
        this.checkNameUnique(req.getName(), req.getParentId(), null);

        // 目录类型菜单，默认为 Layout
        if (MenuType.DIR.equals(req.getType())) {
            req.setComponent(CharSequenceUtil.blankToDefault(req.getComponent(), "Layout"));
        }
        clearMenuCache();
        Menu menuDO = menuAssembler.toEntity(req);
        menuRepository.insert(menuDO);
        return menuDO.getId();
    }

    @Override
    public void update(Long id, MenuRequest req) {

        if(StrUtil.isNotBlank(req.getName())){
            this.checkNameUnique(req.getName(), req.getParentId(), id);
        }


        Menu entity = new Menu();
        menuAssembler.update(req, entity);
        entity.setId(id);
        menuRepository.updateById(entity);
        clearMenuCache();
    }

    /**
     * 删除菜单<br>
     *
     * @param id 菜单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        removeMenus(List.of(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        removeMenus(ids);
    }

    private void removeMenus(List<Long> ids) {
        List<Long> pending = this.listDescendantIds(ids);
        roleMenuRepository.deleteByMenuIds(pending);
        menuRepository.deleteByIds(pending);
        clearMenuCache();
    }

    private void clearMenuCache() {
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    private void assertParent(Map<Long, Menu> byId, Menu node, long parentId) {
        if (parentId == 0L) {
            if (!MenuType.DIR.equals(node.getType())) {
                throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
            }
            return;
        }
        if (parentId == node.getId() || isDescendant(byId, node.getId(), parentId)) {
            throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
        }
        Menu parent = byId.get(parentId);
        if (parent == null) {
            throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
        }
        if (MenuType.DIR.equals(parent.getType())) {
            if (MenuType.BUTTON.equals(node.getType())) {
                throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
            }
            return;
        }
        if (MenuType.MENU.equals(parent.getType()) && MenuType.BUTTON.equals(node.getType())) {
            return;
        }
        throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
    }

    private boolean isDescendant(Map<Long, Menu> byId, Long ancestorId, Long nodeId) {
        Set<Long> guard = new HashSet<>();
        Menu current = byId.get(nodeId);
        while (current != null && current.getParentId() != null && current.getParentId() != 0L) {
            if (!guard.add(current.getId())) {
                return false;
            }
            if (ancestorId.equals(current.getParentId())) {
                return true;
            }
            current = byId.get(current.getParentId());
        }
        return false;
    }

    private long parentOf(Menu menu) {
        return menu.getParentId() == null ? 0L : menu.getParentId();
    }


    /**
     * 检查标题是否重复
     *
     * @param name    标题
     * @param parentId 上级 ID
     * @param selfId   ID
     */
    private void checkNameUnique(String name, Long parentId, Long selfId) {
        if (menuRepository.nameExists(name, parentId, selfId)) {
            throw MenuException.titleExist(name);
        }
    }

    /**
     * 级联获取所有待删除菜单 ID 列表（包含自身及所有子菜单）
     *
     * @param ids ID 列表
     * @return 待删除菜单 ID 列表（包含自身及所有子菜单）
     */
    private List<Long> listDescendantIds(List<Long> ids) {
        List<Long> menuIds = new ArrayList<>(ids);
        List<Long> childIdList = menuRepository.listChildIds(menuIds);
        if (childIdList.isEmpty()) {
            return menuIds;
        }
        menuIds.addAll(this.listDescendantIds(childIdList));
        return menuIds;
    }


    private List<MenuTreeVO> buildPermissionTree(List<Menu> menus) {
        List<MenuTreeVO> flat = menuAssembler.toTreeVOList(menus);
        return TreeUtils.flatToTree(flat,
                MenuTreeVO::getId,
                MenuTreeVO::getParentId,
                MenuTreeVO::getChildren,
                MenuTreeVO::setChildren);
    }


}
