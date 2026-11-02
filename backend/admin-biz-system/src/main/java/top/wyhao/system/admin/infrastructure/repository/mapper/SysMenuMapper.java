
package top.wyhao.system.admin.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMenuDO;
import top.wyhao.system.admin.domain.model.MenuType;
import top.wyhao.mybatis.model.BaseMapper;

import java.util.List;

/**
 * 菜单 Mapper
 *

 * @since 2023/2/15 20:30
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuDO> {

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
    List<SysMenuDO> selectListByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID获取菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.type in (1,2)" +
            "ORDER BY m.sort")
    List<SysMenuDO> selectMenusByUserId(@Param("userId") Long userId);


    default boolean isNameExists(String name, Long selfId){
        return this.lambdaQuery()
                .eq(SysMenuDO::getName, name)
                .ne(SysMenuDO::getType, MenuType.BUTTON)
                .ne(selfId != null, SysMenuDO::getId, selfId)
                .exists();
    }

    default boolean isNameExists(String name, Long parentId, Long selfId) {
        return this.lambdaQuery()
                .eq(SysMenuDO::getName, name)
                .eq(SysMenuDO::getParentId, parentId)
                .ne(SysMenuDO::getType, MenuType.BUTTON)
                .ne(selfId != null, SysMenuDO::getId, selfId)
                .exists();
    }
}
