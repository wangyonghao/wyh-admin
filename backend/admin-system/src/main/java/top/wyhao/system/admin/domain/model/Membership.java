package top.wyhao.system.admin.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成员关系（用户-部门 / 用户-角色 / 用户-组织）。
 */
@Data
@NoArgsConstructor
public class Membership {

    private Long id;

    private Long userId;

    private MembershipScopeType scopeType;

    private Long scopeId;

    private Boolean isPrimary;

    private Integer status;

    private LocalDateTime joinedAt;

    private LocalDateTime expiredAt;

    private Long createUser;

    private LocalDateTime createTime;

    private Long updateUser;

    private LocalDateTime updateTime;

    private Integer deleted;

    public static Membership primaryDepartment(Long userId, Long departmentId) {
        Membership membership = of(userId, MembershipScopeType.DEPARTMENT, departmentId);
        membership.setIsPrimary(true);
        return membership;
    }

    public static Membership role(Long userId, Long roleId) {
        return of(userId, MembershipScopeType.ROLE, roleId);
    }

    public static Membership primaryTenant(Long userId, Long tenantId) {
        Membership membership = of(userId, MembershipScopeType.TENANT, tenantId);
        membership.setIsPrimary(true);
        return membership;
    }

    public static Membership tenant(Long userId, Long tenantId) {
        return of(userId, MembershipScopeType.TENANT, tenantId);
    }

    private static Membership of(Long userId, MembershipScopeType scopeType, Long scopeId) {
        Membership membership = new Membership();
        membership.setUserId(userId);
        membership.setScopeType(scopeType);
        membership.setScopeId(scopeId);
        membership.setIsPrimary(false);
        membership.setStatus(MembershipStatus.NORMAL.getValue());
        membership.setJoinedAt(LocalDateTime.now());
        membership.setDeleted(0);
        return membership;
    }
}
