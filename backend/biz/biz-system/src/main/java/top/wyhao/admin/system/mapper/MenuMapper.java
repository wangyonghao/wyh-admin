
package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.admin.system.entity.SysMenu;
import top.wyhao.admin.system.model.enums.MenuType;
import top.wyhao.cmn.db.model.BaseMapper;

import java.util.List;

/**
 * 菜单 Mapper
 *
 * @author Charles7c
 * @since 2023/2/15 20:30
 */
@Mapper
public interface MenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户 ID 查询权限码
     *
     * @param userId 用户 ID
     * @return 权限码集合
     */
    List<String> selectPermissionByUserId(@Param("userId") Long userId);

    /**
     * 根据角色 ID 查询
     *
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    List<SysMenu> selectListByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID获取菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.type in (1,2)" +
            "ORDER BY m.sort")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);


    default boolean isNameExists(String name, Long selfId){
        return this.lambdaQuery()
                .eq(SysMenu::getName, name)
                .ne(SysMenu::getType, MenuType.BUTTON)
                .ne(selfId != null, SysMenu::getId, selfId)
                .exists();
    }

    default boolean isNameExists(String title, Long parentId, Long selfId) {
        return this.lambdaQuery()
                .eq(SysMenu::getName, title)
                .eq(SysMenu::getParentId, parentId)
                .ne(SysMenu::getType, MenuType.BUTTON)
                .ne(selfId != null, SysMenu::getId, selfId)
                .exists();
    }
}
