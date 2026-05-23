
package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.admin.system.entity.SysRole;
import top.wyhao.cmn.db.model.BaseMapper;

/**
 * 角色 Mapper
 *

 * @since 2023/2/8 23:17
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    default boolean isBuiltIn(Long roleId) {
        return this.lambdaQuery()
                .select(SysRole::getName, SysRole::getIsBuiltin)
                .eq(SysRole::getId, roleId)
                .exists();
    }
}
