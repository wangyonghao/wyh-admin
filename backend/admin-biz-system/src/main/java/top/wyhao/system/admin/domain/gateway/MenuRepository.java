package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.Menu;

import java.util.List;

/**
 * 菜单仓储。
 */
public interface MenuRepository {

    List<Menu> listAll();

    List<Menu> listEnabledTree();

    List<Menu> listEnabledCatalogAndMenu();

    List<Menu> listEnabled();

    List<Menu> listByUserId(Long userId);

    List<Menu> listByRoleId(Long roleId);

    Menu findById(Long id);

    void insert(Menu menu);

    void updateById(Menu menu);

    void updatePlacement(Long id, Long parentId, Integer sort);

    void deleteByIds(List<Long> ids);

    boolean nameExists(String name, Long parentId, Long selfId);

    List<Long> listChildIds(List<Long> parentIds);

    List<String> listPermissionCodesByUserId(Long userId);

    long countIdGreaterThan(Long id);

    boolean deleteIdGreaterThan(Long id);
}
