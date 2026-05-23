
package top.wyhao.admin.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.admin.system.entity.SysRoleDept;
import top.wyhao.cmn.db.model.BaseMapper;

import java.util.List;

/**
 * 角色和部门关联 Mapper
 *

 * @since 2023/2/18 21:57
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    /**
     * 根据角色 ID 查询
     *
     * @param roleId 角色 ID
     * @return 部门 ID 列表
     */
    @Select("SELECT dept_id FROM sys_role_dept WHERE role_id = #{roleId}")
    List<Long> selectDeptIdByRoleId(@Param("roleId") Long roleId);
}
