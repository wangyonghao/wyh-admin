
package top.wyhao.admin.system.mapper.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.admin.system.entity.user.SysUser;
import top.wyhao.admin.system.model.vo.user.UserDetailResult;
import top.wyhao.admin.system.model.vo.user.UserResult;
import top.wyhao.cmn.db.datapermission.annotation.DataPermission;
import top.wyhao.cmn.db.datapermission.mapper.DataPermissionMapper;

import java.util.List;

/**
 * 用户 Mapper
 *
 * @author Charles7c
 * @since 2022/12/22 21:47
 */
@Mapper
public interface UserMapper extends DataPermissionMapper<SysUser> {

    /**
     * 分页查询列表
     *
     * @param page         分页条件
     * @param queryWrapper 查询条件
     * @return 分页列表信息
     */
    @DataPermission(tableAlias = "t1")
    IPage<UserResult> selectUserPage(@Param("page") IPage<SysUser> page,
                                     @Param(Constants.WRAPPER) QueryWrapper<SysUser> queryWrapper);

    /**
     * 查询列表
     *
     * @param queryWrapper 查询条件
     * @return 列表信息
     */
    @DataPermission(tableAlias = "t1")
    List<UserDetailResult> selectUserList(@Param(Constants.WRAPPER) QueryWrapper<SysUser> queryWrapper);

    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone}")
    SysUser selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email}")
    SysUser selectByEmail(@Param("email") String email);

    /**
     * 根据 ID 查询昵称
     *
     * @param id ID
     * @return 昵称
     */
    @Select("SELECT nickname FROM sys_user WHERE id = #{id}")
    String selectNicknameById(@Param("id") Long id);


    /**
     * 根据用户ID获取角色编码列表
     */
    @Select("SELECT r.code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

}
