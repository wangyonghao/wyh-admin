
package top.wyhao.system.admin.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleDepartmentDO;
import top.wyhao.mybatis.model.BaseMapper;

import java.util.List;

/**
 * 角色和部门关联 Mapper
 *

 * @since 2023/2/18 21:57
 */
@Mapper
public interface SysRoleDepartmentMapper extends BaseMapper<SysRoleDepartmentDO> {

    /**
     * 根据角色 ID 查询
     *
     * @param roleId 角色 ID
     * @return 部门 ID 列表
     */
    @Select("SELECT department_id FROM sys_role_department WHERE role_id = #{roleId}")
    List<Long> selectDepartmentIdByRoleId(@Param("roleId") Long roleId);
}
