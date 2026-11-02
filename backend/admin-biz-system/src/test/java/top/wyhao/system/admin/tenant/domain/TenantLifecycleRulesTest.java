package top.wyhao.system.admin.tenant.domain;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.domain.TenantLifecycleRules;

import static org.assertj.core.api.Assertions.assertThat;

class TenantLifecycleRulesTest {

    @Test
    void defaultTenantCannotBeDeleted() {
        assertThat(TenantLifecycleRules.deletable(0L, 0L)).isFalse();
        assertThat(TenantLifecycleRules.deletable(null, 0L)).isFalse();
    }

    @Test
    void businessTenantCanBeDeleted() {
        assertThat(TenantLifecycleRules.deletable(9L, 0L)).isTrue();
    }
}
