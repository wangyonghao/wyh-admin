/*
 * Copyright (c) 2022-present wangyonghao Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.wyhao.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.model.SystemConstants;
import top.wyhao.admin.system.model.bo.MenuRequest;
import top.wyhao.admin.system.model.entity.MenuDO;
import top.wyhao.admin.system.model.enums.MenuType;
import top.wyhao.admin.system.model.query.MenuQuery;
import top.wyhao.admin.system.model.vo.MenuTreeVO;
import top.wyhao.admin.system.model.vo.MenuVO;
import top.wyhao.admin.system.mapper.MenuMapper;
import top.wyhao.admin.system.service.MenuService;
import top.wyhao.admin.system.service.UserService;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.enums.RoleCodeEnum;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.util.TreeUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.data.util.QueryWrapperUtil;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单 Service
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final UserService userService;
    private final MenuMapper menuMapper;

    @Override
    public List<MenuTreeVO> tree(MenuQuery query) {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuDO::getStatus, StatusEnum.ENABLE.getValue())
                .orderByAsc(MenuDO::getParentId)
                .orderByAsc(MenuDO::getSort);

        if (query != null) {
            QueryWrapperUtil.applySort(wrapper, query.getSort(), MenuDO.class);
        }
        List<MenuDO> menus = menuMapper.selectList(wrapper);
        return buildPermissionTree(menus);
    }

    @Override
    public List<MenuTreeVO> getMenuTreeByUserId(Long userId) {
        // 获取用户的角色ID列表
        List<String> roleCodes = userService.findUserRoles(userId);
        // 超级管理员
        if (roleCodes.contains(RoleCodeEnum.SUPER_ADMIN.getCode())) {
            LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuDO::getStatus, 1)
                    .in(MenuDO::getType, 1, 2)
                    .orderByAsc(MenuDO::getParentId)
                    .orderByAsc(MenuDO::getSort);
            List<MenuDO> menus = menuMapper.selectList(wrapper);
            return this.buildPermissionTree(menus);
        }
        // 普通用户
        List<MenuDO> menus = menuMapper.selectMenusByUserId(userId);
        return this.buildPermissionTree(menus);
    }

    @Override
    public List<MenuVO> listByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        // 如果包含超级管理员角色，则返回所有启用的菜单
        if (roleIds.contains(SystemConstants.SUPER_ADMIN_ROLE_ID)) {
            List<MenuDO> menuList = menuMapper.lambdaQuery()
                    .eq(MenuDO::getStatus, "1")
                    .list();
            return BeanUtil.copyToList(menuList, MenuVO.class);
        }

        // 否则根据角色ID列表获取菜单 - 遍历每个角色ID并合并菜单列表
        List<MenuDO> allMenus = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<MenuDO> menusForRole = menuMapper.selectListByRoleId(roleId);
            allMenus.addAll(menusForRole);
        }

        // 去重并转换为响应对象
        return BeanUtil.copyToList(allMenus.stream().distinct().toList(), MenuVO.class);
    }


    @Override
    public List<MenuVO> list(MenuQuery query, SortQuery sortQuery) {
        return List.of();
    }

    @Override
    public void export(MenuQuery query, SortQuery sortQuery, HttpServletResponse response) {

    }


    @Override
    public MenuVO get(Long id) {
        MenuDO menuDO = menuMapper.selectById(id);
        BizAssert.isNull(menuDO, "菜单不存在");
        return BeanUtil.copyProperties(menuDO, MenuVO.class);
    }

    @Override
    public Long create(MenuRequest req) {
        this.checkNameUnique(req.getName(), req.getParentId(), null);

        // 目录类型菜单，默认为 Layout
        if (MenuType.DIR.equals(req.getType())) {
            req.setComponent(CharSequenceUtil.blankToDefault(req.getComponent(), "Layout"));
        }
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
        MenuDO menuDO = BeanUtil.copyProperties(req, MenuDO.class);
        menuMapper.insert(menuDO);
        return menuDO.getId();
    }

    @Override
    public void update(Long id, MenuRequest req) {
        this.checkNameUnique(req.getName(), req.getParentId(), id);
        MenuDO oldMenu = menuMapper.selectById(id);
        BizAssert.throwIfNotEqual(req.getType(), oldMenu.getType(), "不允许修改菜单类型");

        MenuDO entity = BeanUtil.copyProperties(req, MenuDO.class);
        entity.setId(id);
        menuMapper.updateById(entity);
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    /**
     * 删除菜单<br>
     *
     * @param id 菜单ID
     */
    @Override
    public void delete(Long id) {
        menuMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        // 级联删除菜单（包含子菜单）
        List<Long> allDeleteIdList = this.listCascadingDeleteMenuIds(ids);
        menuMapper.deleteByIds(allDeleteIdList);
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }


    /**
     * 检查标题是否重复
     *
     * @param title    标题
     * @param parentId 上级 ID
     * @param selfId   ID
     */
    private void checkNameUnique(String title, Long parentId, Long selfId) {
        BizAssert.isTrue(menuMapper.isNameExists(title, parentId, selfId), "标题为 [{}] 的菜单已存在", title);
    }

    /**
     * 级联获取所有待删除菜单 ID 列表（包含自身及所有子菜单）
     *
     * @param ids ID 列表
     * @return 待删除菜单 ID 列表（包含自身及所有子菜单）
     */
    private List<Long> listCascadingDeleteMenuIds(List<Long> ids) {
        List<Long> menuIds = new ArrayList<>(ids);
        List<Long> childIdList = menuMapper.lambdaQuery()
                .select(MenuDO::getId)
                .in(MenuDO::getParentId, menuIds)
                .list()
                .stream()
                .map(MenuDO::getId)
                .toList();
        if (childIdList.isEmpty()) {
            return menuIds;
        }
        menuIds.addAll(this.listCascadingDeleteMenuIds(childIdList));
        return menuIds;
    }


    private List<MenuTreeVO> buildPermissionTree(List<MenuDO> menus) {
        List<MenuTreeVO> flat = BeanUtil.copyToList(menus, MenuTreeVO.class);
        return TreeUtils.flatToTree(flat,
                MenuTreeVO::getId,
                MenuTreeVO::getParentId,
                MenuTreeVO::getChildren,
                MenuTreeVO::setChildren);
    }


}
