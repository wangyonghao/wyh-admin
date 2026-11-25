package top.wyhao.system.admin.domain.catalog;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.domain.exception.PackageException;
import top.wyhao.system.admin.domain.model.Menu;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackageCatalogRulesTest {

    @Test
    void shouldFollowLifecycle() {
        assertThatThrownBy(() -> PackageCatalogRules.assertRename(true))
                .isInstanceOf(PackageException.class)
                .extracting(ex -> ((PackageException) ex).getCode())
                .isEqualTo("PACKAGE_NAME_LOCKED");
        assertThatThrownBy(() -> PackageCatalogRules.assertEditMenus("DELISTED"))
                .isInstanceOf(PackageException.class);
        assertThatThrownBy(() -> PackageCatalogRules.assertDelete("PUBLISHED", true))
                .isInstanceOf(PackageException.class);
        assertThatThrownBy(() -> PackageCatalogRules.assertDelist("PUBLISHED", true))
                .isInstanceOf(PackageException.class)
                .extracting(ex -> ((PackageException) ex).getCode())
                .isEqualTo("PACKAGE_DIRTY");
        assertThatCode(() -> PackageCatalogRules.assertDelist("PUBLISHED", false)).doesNotThrowAnyException();
        assertThatCode(() -> PackageCatalogRules.assertRelist("DELISTED")).doesNotThrowAnyException();
        assertThatThrownBy(() -> PackageCatalogRules.assertPublish("DELISTED"))
                .isInstanceOf(PackageException.class);
    }

    @Test
    void shouldKeepRequiredPagesAndTheirButtons() {
        List<PackageMenuRef> menus = List.of(
                new PackageMenuRef("home", "0", 2, true),
                new PackageMenuRef("order", "biz", 2, false),
                new PackageMenuRef("btn", "order", 3, false),
                new PackageMenuRef("biz", "0", 1, false));

        Set<String> selected = PackageCatalogRules.normalizeSelection(Set.of("order", "biz"), menus);

        assertThat(selected).containsExactlyInAnyOrder("home", "order", "btn");
    }

    @Test
    void shouldDetectPackageDifferenceOnAPageAndItsDirectory() {
        Map<String, Set<String>> selections = Map.of(
                "basic", Set.of("home", "order"),
                "pro", Set.of("home", "order", "report"));

        assertThat(PackageCatalogRules.differs(List.of("order"), "basic", selections)).isFalse();
        assertThat(PackageCatalogRules.differs(List.of("report"), "basic", selections)).isTrue();
        assertThat(PackageCatalogRules.differs(List.of("order", "report"), "basic", selections)).isTrue();
    }

    @Test
    void shouldKeepTenantDirectoriesThatStillHaveAVisiblePage() {
        Menu root = menu(1L, 0L, "TENANT");
        Menu page = menu(2L, 1L, "TENANT");
        Menu hidden = menu(3L, 1L, "TENANT");
        Menu platform = menu(9L, 0L, "PLATFORM");

        List<Menu> visible = MenuAccess.tenant(List.of(root, page, hidden, platform), Set.of(2L));

        assertThat(visible).extracting(Menu::getId).containsExactly(1L, 2L);
        assertThat(MenuAccess.platform(List.of(root, platform))).extracting(Menu::getId).containsExactly(9L);
    }

    private static Menu menu(Long id, Long parentId, String scope) {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setScope(scope);
        return menu;
    }
}
