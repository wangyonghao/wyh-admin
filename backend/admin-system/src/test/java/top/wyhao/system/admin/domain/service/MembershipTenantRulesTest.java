package top.wyhao.system.admin.domain.service;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.domain.model.MembershipScopeType;
import top.wyhao.system.admin.domain.model.MembershipStatus;
import top.wyhao.system.admin.domain.model.Membership;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MembershipTenantRulesTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 10, 17, 12, 0);

    @Test
    void primaryTenantMembershipIsEffectiveWhenNormal() {
        Membership membership = Membership.primaryTenant(1L, 9L);
        assertThat(membership.getScopeType()).isEqualTo(MembershipScopeType.TENANT);
        assertThat(membership.getIsPrimary()).isTrue();
        assertThat(MembershipRules.isEffective(membership, now)).isTrue();
    }

    @Test
    void invitedTenantMembershipNotEffective() {
        Membership membership = Membership.tenant(1L, 9L);
        membership.setStatus(MembershipStatus.INVITED.getValue());
        assertThat(MembershipRules.isEffective(membership, now)).isFalse();
    }
}
