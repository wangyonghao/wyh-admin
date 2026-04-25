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

package top.wyhao.admin.system.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.admin.system.model.bo.MenuRequest;
import top.wyhao.admin.system.model.query.MenuQuery;
import top.wyhao.admin.system.model.vo.MenuTreeVO;
import top.wyhao.admin.system.model.vo.MenuVO;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.List;

/**
 * 菜单业务接口
 *
 * @author Charles7c
 * @since 2023/2/15 20:30
 */
public interface MenuService {

    List<MenuTreeVO> tree(@Valid MenuQuery query);

    MenuVO get(Long id);

    Long create(@Valid MenuRequest req);

    void update(Long id, @Valid MenuRequest req);

    void delete(Long id);

    void delete(List<Long> id);

    /**
     * 根据用户ID获取菜单树
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<MenuTreeVO> getMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID列表获取菜单列表
     *
     * @param roleIds 角色ID列表
     * @return 菜单列表
     */
    List<MenuVO> listByRoleIds(List<Long> roleIds);

    List<MenuVO> list(@Valid MenuQuery query, @Valid SortQuery sortQuery);

    void export(@Valid MenuQuery query, @Valid SortQuery sortQuery, HttpServletResponse response);


}
