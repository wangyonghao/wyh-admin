package top.wyhao.system.admin.domain.service;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.domain.model.MembershipStatus;
import top.wyhao.system.admin.domain.model.Membership;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MembershipRules} 有效成员判定。
 */
class MembershipRulesTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 10, 6, 12, 0);

    @Test
    void effectiveWhenNormalAndNotExpired() {
        Membership membership = active();
        assertThat(MembershipRules.isEffective(membership, now)).isTrue();
    }

    @Test
    void ineffectiveWhenDeleted() {
        Membership membership = active();
        membership.setDeleted(1);
        assertThat(MembershipRules.isEffective(membership, now)).isFalse();
    }

    @Test
    void ineffectiveWhenDisabledOrInvited() {
        Membership disabled = active();
        disabled.setStatus(MembershipStatus.DISABLED.getValue());
        assertThat(MembershipRules.isEffective(disabled, now)).isFalse();

        Membership invited = active();
        invited.setStatus(MembershipStatus.INVITED.getValue());
        assertThat(MembershipRules.isEffective(invited, now)).isFalse();
    }

    @Test
    void ineffectiveWhenExpired() {
        Membership membership = active();
        membership.setExpiredAt(now.minusSeconds(1));
        assertThat(MembershipRules.isEffective(membership, now)).isFalse();
    }

    @Test
    void effectiveWhenExpiredAtInFuture() {
        Membership membership = active();
        membership.setExpiredAt(now.plusDays(1));
        assertThat(MembershipRules.isEffective(membership, now)).isTrue();
    }

    @Test
    void ineffectiveWhenNull() {
        assertThat(MembershipRules.isEffective(null, now)).isFalse();
    }

    private static Membership active() {
        Membership membership = new Membership();
        membership.setDeleted(0);
        membership.setStatus(MembershipStatus.NORMAL.getValue());
        membership.setExpiredAt(null);
        return membership;
    }
}
