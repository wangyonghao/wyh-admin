
package top.wyhao.system.admin.infrastructure.repository.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleMenuDO;
import top.wyhao.mybatis.model.BaseMapper;

import java.util.List;

/**
 * 角色和菜单 Mapper
 *

 * @since 2023/2/15 20:30
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenuDO> {

    /**
     * 根据角色 ID 列表查询
     *
     * @param roleIds 角色 ID 列表
     * @return 菜单 ID 列表
     */
    List<Long> selectMenuIdByRoleIds(List<Long> roleIds);
}
