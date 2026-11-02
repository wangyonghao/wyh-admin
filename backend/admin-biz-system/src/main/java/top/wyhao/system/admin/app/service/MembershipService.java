package top.wyhao.system.admin.app.service;

import top.wyhao.system.admin.domain.model.Membership;

import java.util.Collection;
import java.util.List;

/**
 * 成员关系服务（双写阶段：与 department_id / user_role 同步）。
 */
public interface MembershipService {

    void replacePrimaryDepartment(Long userId, Long departmentId);

    void replacePrimaryDepartments(List<long[]> userDepartmentPairs);

    void replaceRoles(Long userId, List<Long> roleIds);

    void addRoleMembers(Long roleId, Collection<Long> userIds);

    void removeRoleMembers(Long roleId, Collection<Long> userIds);

    void removeAllRolesByUserIds(Collection<Long> userIds);

    void removeAllByUserIds(Collection<Long> userIds);

    /**
     * 确保用户加入组织；已存在则保持。primary=true 时设为主组织。
     */
    void ensureTenantMembership(Long userId, Long tenantId, boolean primary);

    /**
     * 将指定组织设为主组织（须已是成员）。
     */
    void setPrimaryTenant(Long userId, Long tenantId);

    /**
     * 解析登录应进入的主组织 ID；无组织返回 null。
     */
    Long resolvePrimaryTenantId(Long userId);

    List<Membership> listEffectiveTenantMemberships(Long userId);

    boolean isTenantMember(Long userId, Long tenantId);
}
