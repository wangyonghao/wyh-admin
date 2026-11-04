
package top.wyhao.system.admin.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleDO;

import java.util.List;

/**
 * 角色 Mapper
 *

 * @since 2023/2/8 23:17
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleDO> {

    /**
     * 根据用户 ID 查询角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<SysRoleDO> selectRolesByUserId(@Param("userId") Long userId);

    default boolean isBuiltIn(Long roleId) {
        return this.lambdaQuery()
                .select(SysRoleDO::getName, SysRoleDO::getIsBuiltin)
                .eq(SysRoleDO::getId, roleId)
                .exists();
    }

    default boolean isNameExists(String name, Long selfId) {
        return this.lambdaQuery()
                .eq(SysRoleDO::getName, name)
                .ne(selfId != null, SysRoleDO::getId, selfId)
                .exists();
    }
}
