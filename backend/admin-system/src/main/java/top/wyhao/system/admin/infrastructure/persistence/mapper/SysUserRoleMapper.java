
package top.wyhao.system.admin.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysUserRoleDO;
import top.wyhao.mybatis.model.BaseMapper;
import top.wyhao.system.admin.app.dto.RoleMemberResult;

import java.util.List;

/**
 * 用户和角色 Mapper
 *

 * @since 2023/2/13 23:13
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRoleDO> {

    /**
     * 分页查询列表
     *
     * @param page         分页条件
     * @param queryWrapper 查询条件
     * @return 分页列表信息
     */
    IPage<RoleMemberResult> selectUserPage(@Param("page") IPage<SysUserRoleDO> page,
                                        @Param(Constants.WRAPPER) QueryWrapper<SysUserRoleDO> queryWrapper);

    /**
     * 根据用户 ID 获取角色编码
     */
    @Select("SELECT r.code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
