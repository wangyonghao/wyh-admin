
package top.wyhao.system.admin.app.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.system.admin.app.dto.MenuTreeVO;
import top.wyhao.system.admin.app.dto.MenuVO;

import java.util.List;
import top.wyhao.system.admin.app.dto.MenuQuery;
import top.wyhao.system.admin.app.dto.MenuRequest;
import top.wyhao.system.admin.app.dto.MenuSortRequest;

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

    List<MenuVO> list(@Valid MenuQuery query);

    List<MenuVO> listAll();

    void sort(@Valid MenuSortRequest req);

    void export(@Valid MenuQuery query, HttpServletResponse response);


}
