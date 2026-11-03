package top.wyhao.security.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 角色业务 API（授权域对外提供）
 */
public interface RoleApi {

    /**
     * 根据编码查询 ID
     *
     * @param code 编码
     * @return 角色 ID
     */
    Long getIdByCode(String code);

    /**
     * 更新用户上下文
     *
     * @param roleId 角色 ID
     */
    void updateUserContext(Long roleId);

    /**
     * 根据角色名称查询数量
     */
    int countByNames(List<String> roleNames);

    /**
     * 根据角色名称映射 ID
     */
    Map<String, Long> mapIdByNames(List<String> roleNames);

    /**
     * 批量分配角色给指定用户（覆盖式）
     */
    boolean assignRolesToUser(List<Long> roleIds, Long userId);

    /**
     * 查询角色下成员用户 ID
     */
    List<Long> listMemberIds(Long roleId);

    /**
     * 删除用户的全部角色关联
     */
    void deleteUserRolesByUserIds(Collection<Long> userIds);


}
