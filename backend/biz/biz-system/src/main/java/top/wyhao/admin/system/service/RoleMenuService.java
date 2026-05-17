
package top.wyhao.admin.system.service;

import top.wyhao.admin.system.entity.SysRoleMenu;
import top.wyhao.cmn.db.model.IService;

import java.util.List;

/**
 * 角色和菜单业务接口
 *
 * @author Charles7c
 * @since 2023/2/19 10:40
 */
public interface RoleMenuService extends IService<SysRoleMenu> {

    /**
     * 新增
     *
     * @param menuIds 菜单 ID 列表
     * @param roleId  角色 ID
     * @return 是否新增成功（true：成功；false：无变更/失败）
     */
    boolean save(List<Long> menuIds, Long roleId);

    /**
     * 根据角色 ID 列表删除
     *
     * @param roleId 角色 ID 列表
     */
    void deleteByRoleId(Long roleId);

    /**
     * 根据角色 ID 列表查询
     *
     * @param roleIds 角色 ID 列表
     * @return 菜单 ID 列表
     */
    List<Long> listMenuIdByRoleIds(List<Long> roleIds);
}