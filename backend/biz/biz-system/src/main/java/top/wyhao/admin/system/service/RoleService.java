
package top.wyhao.admin.system.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.admin.system.model.bo.RolePermissionUpdateRequest;
import top.wyhao.admin.system.model.bo.RoleRequest;
import top.wyhao.admin.system.entity.SysRole;
import top.wyhao.admin.system.model.query.RoleQuery;
import top.wyhao.admin.system.model.query.RoleUserQuery;
import top.wyhao.admin.system.model.vo.MenuVO;
import top.wyhao.admin.system.model.vo.role.RoleDetailResult;
import top.wyhao.admin.system.model.vo.role.RoleResult;
import top.wyhao.admin.system.model.vo.role.RoleUserResult;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.List;

/**
 * 角色业务接口
 *

 * @since 2023/2/8 23:15
 */
public interface RoleService {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<RoleResult> page(@Valid RoleQuery query, @Valid PageQuery pageQuery);

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 列表信息
     */
    List<RoleResult> list(@Valid RoleQuery query, @Valid SortQuery sortQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    RoleDetailResult detail(Long id);

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return 自增 ID
     */
    Long create(@Valid RoleRequest req);

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(@Valid RoleRequest req, Long id);

    /**
     * 删除
     *
     * @param ids ID 列表
     */
    void delete(Long id);

    /**
     * 导出
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void export(@Valid RoleQuery query, @Valid SortQuery sortQuery, HttpServletResponse response);

    /**
     * 修改角色权限
     *
     * @param id  角色 ID
     * @param req 请求参数
     */
    void updatePermission(Long id, RolePermissionUpdateRequest req);

    /**
     * 分配角色给用户
     *
     * @param id      角色 ID
     * @param userIds 用户 ID 列表
     */
    void assignToUsers(Long id, List<Long> userIds);

    /**
     * 根据角色名称查询
     *
     * @param list 名称列表
     * @return 角色列表
     */
    List<SysRole> listByNames(List<String> list);

    /**
     * 根据角色名称查询数量
     *
     * @param roleNames 名称列表
     * @return 角色数量
     */
    int countByNames(List<String> roleNames);

    /**
     * 批量分配角色给指定用户
     *
     * @param roleIds 角色 ID 列表
     * @param userId  用户 ID
     * @return 是否成功（true：成功；false：无变更/失败）
     */
    boolean assignRolesToUser(List<Long> roleIds, Long userId);

    /**
     * 根据用户 ID 查询
     *
     * @param userId 用户 ID
     * @return 角色 ID 列表
     */
    List<Long> findRoleIdsByUserId(Long userId);

    /**
     * 根据角色 ID 查询
     *
     * @param roleId 角色 ID
     * @return 用户 ID 列表
     */
    List<Long> listMemberIds(Long roleId);

    Long getIdByCode(String code);

    void updateUserContext(Long roleId);

    /**
     * 根据角色 ID 查询
     *
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    List<MenuVO> listMenuByRoleId(Long roleId);

    /**
     * 根据角色 ID 查询用户列表
     *
     * @param roleId 角色 ID
     * @return 用户列表
     */
    List<RoleUserResult> pageMember(Long roleId, RoleUserQuery query);


    void deleteMember(Long roleId, List<Long> ids);
}