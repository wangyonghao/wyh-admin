package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.UserRole;

import java.util.Collection;
import java.util.List;

/**
 * 用户与角色关联仓储。
 */
public interface UserRoleRepository {

    boolean insertBatch(List<UserRole> userRoles);

    boolean existsByRoleId(Long roleId);

    List<UserRole> listByUserId(Long userId);

    List<Long> listUserIdsByRoleId(Long roleId);

    void deleteByUserId(Long userId);

    void deleteByUserIds(List<Long> userIds);

    void deleteByRoleIdAndUserIds(Long roleId, List<Long> userIds);

    List<String> listRoleCodesByUserId(Long userId);

    void deleteAll();

    boolean deleteByRoleIdNotIn(Collection<Long> keepRoleIds);

    boolean deleteByUserIdNotIn(Collection<Long> keepUserIds);
}
