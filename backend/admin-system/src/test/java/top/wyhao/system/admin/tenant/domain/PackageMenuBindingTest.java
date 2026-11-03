package top.wyhao.system.admin.tenant.domain;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.domain.PackageMenuBinding;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 套餐菜单原样授予租户管理员：顺序无关，重复项折叠，增删都算变更。
 */
class PackageMenuBindingTest {

    @Test
    void sameMenusInDifferentOrderAreUnchanged() {
        assertThat(PackageMenuBinding.changed(List.of(1010L, 1011L, 1000L), List.of(1000L, 1011L, 1010L)))
            .isFalse();
        assertThat(PackageMenuBinding.changed(List.of(1010L, 1010L), List.of(1010L))).isFalse();
        assertThat(PackageMenuBinding.changed(null, List.of())).isFalse();
    }

    @Test
    void addedOrRemovedMenuIsAChange() {
        assertThat(PackageMenuBinding.changed(List.of(1010L, 1011L), List.of(1010L))).isTrue();
        assertThat(PackageMenuBinding.changed(List.of(1010L), List.of(1010L, 1070L))).isTrue();
    }

    @Test
    void tenantAdminReceivesExactlyThePackageMenus() {
        assertThat(PackageMenuBinding.menusForTenantAdmin(Arrays.asList(1000L, 1010L, 1010L, null, 1011L)))
            .containsExactly(1000L, 1010L, 1011L);
        assertThat(PackageMenuBinding.menusForTenantAdmin(null)).isEmpty();
    }
}
