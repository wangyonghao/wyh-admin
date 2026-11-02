
package top.wyhao.system.admin.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wyhao.system.admin.app.dto.UserDetail;
import top.wyhao.system.admin.app.dto.UserResult;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysUserDO;
import top.wyhao.mybatis.datapermission.annotation.DataScope;
import top.wyhao.mybatis.datapermission.mapper.DataPermissionMapper;

import java.util.List;

/**
 * 用户 Mapper
 *

 * @since 2022/12/22 21:47
 */
@Mapper
public interface SysUserMapper extends DataPermissionMapper<SysUserDO> {

    /**
     * 分页查询列表
     *
     * @param page         分页条件
     * @param queryWrapper 查询条件
     * @return 分页列表信息
     */
    @DataScope(userAlias = "t1")
    IPage<UserResult> selectUserPage(@Param("page") IPage<SysUserDO> page,
                                           @Param(Constants.WRAPPER) Wrapper<SysUserDO> queryWrapper);

    /**
     * 查询列表
     *
     * @param queryWrapper 查询条件
     * @return 列表信息
     */
    @DataScope(userAlias = "t1")
    List<UserDetail> selectUserList(@Param(Constants.WRAPPER) Wrapper<SysUserDO> queryWrapper);

    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUserDO selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone}")
    SysUserDO selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email}")
    SysUserDO selectByEmail(@Param("email") String email);

    /**
     * 根据 ID 查询显示名称
     *
     * @param id ID
     * @return 显示名称
     */
    @Select("SELECT display_name FROM sys_user WHERE id = #{id}")
    String selectDisplayNameById(@Param("id") Long id);


}
