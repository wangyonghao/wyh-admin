
package top.wyhao.admin.system.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.admin.system.entity.SysUserRole;
import top.wyhao.admin.system.model.vo.role.RoleUserResult;
import top.wyhao.cmn.db.model.BaseMapper;

/**
 * 用户和角色 Mapper
 *
 * @author Charles7c
 * @since 2023/2/13 23:13
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 分页查询列表
     *
     * @param page         分页条件
     * @param queryWrapper 查询条件
     * @return 分页列表信息
     */
    IPage<RoleUserResult> selectUserPage(@Param("page") IPage<SysUserRole> page,
                                         @Param(Constants.WRAPPER) QueryWrapper<SysUserRole> queryWrapper);



}
