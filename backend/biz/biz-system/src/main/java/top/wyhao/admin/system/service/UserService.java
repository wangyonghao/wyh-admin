
package top.wyhao.admin.system.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.admin.system.model.bo.user.*;
import top.wyhao.admin.system.entity.SysUser;
import top.wyhao.admin.system.model.query.UserQuery;
import top.wyhao.admin.system.model.vo.user.UserDetailResult;
import top.wyhao.admin.system.model.vo.user.UserImportParseResp;
import top.wyhao.admin.system.model.vo.user.UserImportResp;
import top.wyhao.admin.system.model.vo.user.UserResult;
import top.wyhao.starter.core.spi.PermissionProvider;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.io.IOException;
import java.util.List;

/**
 * 用户业务接口
 */
public interface UserService {


    /**
     * 解析导入数据
     *
     * @param file 导入文件
     * @return 解析结果
     */
    UserImportParseResp parseImport(MultipartFile file);

    /**
     * 导入数据
     *
     * @param req 请求参数
     * @return 导入结果
     */
    UserImportResp importUser(UserImportRequest req);

    /**
     * 重置密码
     *
     * @param req 请求参数
     * @param id  ID
     */
    void resetPassword(UserPasswordResetRequest req, Long id);

    /**
     * 重置密码（生成随机密码）
     *
     * @param id 用户ID
     * @return 新密码
     */
    String resetPassword(Long id);

    /**
     * 修改角色
     *
     * @param updateReq 修改信息
     * @param id        ID
     */
    void updateRole(UserRoleUpdateReq updateReq, Long id);

    /**
     * 上传头像
     *
     * @param avatar 头像文件
     * @param id     ID
     * @return 新头像路径
     * @throws IOException /
     */
    String updateAvatar(MultipartFile avatar, Long id) throws IOException;

    /**
     * 修改基础信息
     *
     * @param req 修改信息
     * @param id  ID
     */
    void updateBasicInfo(UserBasicInfoUpdateReq req, Long id);

    /**
     * 修改密码
     *
     * @param oldPassword 当前密码
     * @param newPassword 新密码
     * @param id          ID
     */
    void updatePassword(String oldPassword, String newPassword, Long id);

    /**
     * 修改手机号
     *
     * @param newPhone    新手机号
     * @param oldPassword 当前密码
     * @param id          ID
     */
    void updatePhone(String newPhone, String oldPassword, Long id);

    /**
     * 修改邮箱
     *
     * @param newEmail    新邮箱
     * @param oldPassword 当前密码
     * @param id          ID
     */
    void updateEmail(String newEmail, String oldPassword, Long id);

    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 根据手机号查询
     *
     * @param phone 手机号
     * @return 用户信息
     */
    SysUser getByPhone(String phone);

    /**
     * 根据邮箱查询
     *
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser getByEmail(String email);

    /**
     * 根据部门 ID 列表查询
     *
     * @param deptIds 部门 ID 列表
     * @return 用户数量
     */
    Long countByDeptIds(List<Long> deptIds);

    UserDetailResult detail(Long id);

    PageResult<UserResult> page(UserQuery query, PageQuery pageQuery);

    Long save(SysUser user);

    Long create(@Valid UserRequest req);

    void export(@Valid UserQuery query, @Valid SortQuery sortQuery, HttpServletResponse response);

    void update(Long id, @Valid UserRequest req);

    void delete(List<Long> id);

    /**
     * 获取用户权限码集合
     *
     * @param userId 用户ID
     * @return 权限码集合
     */
    List<String> findUserPermissions(Long userId);

    /**
     * 获取用户角色码集合
     *
     * @param userId 用户ID
     * @return 角色码集合
     */
    List<String> findUserRoles(Long userId);
}
