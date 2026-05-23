
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
