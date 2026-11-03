package top.wyhao.system.admin.domain.service;

import top.wyhao.system.admin.domain.model.MembershipStatus;
import top.wyhao.system.admin.domain.model.Membership;

import java.time.LocalDateTime;

/**
 * 成员关系领域规则。
 */
public final class MembershipRules {

    private MembershipRules() {
    }

    /**
     * 是否为有效成员：未删除、状态正常、未过期。
     */
    public static boolean isEffective(Membership membership, LocalDateTime now) {
        if (membership == null) {
            return false;
        }
        if (membership.getDeleted() != null && membership.getDeleted() != 0) {
            return false;
        }
        if (membership.getStatus() == null || membership.getStatus() != MembershipStatus.NORMAL.getValue()) {
            return false;
        }
        LocalDateTime expiredAt = membership.getExpiredAt();
        return expiredAt == null || expiredAt.isAfter(now);
    }
}
