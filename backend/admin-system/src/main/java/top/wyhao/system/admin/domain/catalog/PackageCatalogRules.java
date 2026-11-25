package top.wyhao.system.admin.domain.catalog;

import top.wyhao.system.admin.domain.exception.PackageException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 套餐状态和菜单勾选。套餐只决定包含哪些页面，目录由可见子页面带出来。
 */
public final class PackageCatalogRules {

    private PackageCatalogRules() {
    }

    public static void assertRename(boolean everPublished) {
        if (everPublished) {
            throw PackageException.nameLocked();
        }
    }

    public static void assertEditMenus(String lifecycle) {
        if ("DELISTED".equals(lifecycle)) {
            throw PackageException.of("PACKAGE_LIFECYCLE", "已下架套餐不能修改菜单");
        }
    }

    public static void assertDelete(String lifecycle, boolean everPublished) {
        if (everPublished || !"DRAFT".equals(lifecycle)) {
            throw PackageException.of("PACKAGE_LIFECYCLE", "只有未发布的套餐可以删除");
        }
    }

    public static void assertPublish(String lifecycle) {
        if ("DELISTED".equals(lifecycle)) {
            throw PackageException.of("PACKAGE_LIFECYCLE", "已下架套餐不能发布，请先重新上架");
        }
    }

    public static void assertDelist(String lifecycle, boolean dirty) {
        if (!"PUBLISHED".equals(lifecycle)) {
            throw PackageException.of("PACKAGE_LIFECYCLE", "只有已发布套餐可以下架");
        }
        if (dirty) {
            throw PackageException.of("PACKAGE_DIRTY", "有待发布变更，请先发布或放弃");
        }
    }

    public static void assertRelist(String lifecycle) {
        if (!"DELISTED".equals(lifecycle)) {
            throw PackageException.of("PACKAGE_LIFECYCLE", "只有已下架套餐可以重新上架");
        }
    }

    public static Set<String> normalizeSelection(Set<String> requested, List<PackageMenuRef> menus) {
        Set<String> pages = new HashSet<>();
        Set<String> required = new HashSet<>();
        for (PackageMenuRef menu : menus) {
            if (menu.type() == 2) {
                pages.add(menu.id());
                if (menu.required()) {
                    required.add(menu.id());
                }
            }
        }
        Set<String> chosen = new HashSet<>(required);
        if (requested != null) {
            for (String id : requested) {
                if (pages.contains(id)) {
                    chosen.add(id);
                }
            }
        }
        Set<String> result = new HashSet<>(chosen);
        for (PackageMenuRef menu : menus) {
            if (menu.type() == 3 && chosen.contains(menu.parentId())) {
                result.add(menu.id());
            }
        }
        return result;
    }

    /**
     * 相对基准套餐，这一行（页面自身，或目录下的页面）是否有套餐不一致。
     */
    public static boolean differs(List<String> pageIds, String basePackageId, Map<String, Set<String>> selections) {
        Set<String> base = selections.getOrDefault(basePackageId, Set.of());
        for (Map.Entry<String, Set<String>> entry : selections.entrySet()) {
            if (entry.getKey().equals(basePackageId)) {
                continue;
            }
            for (String pageId : pageIds) {
                if (base.contains(pageId) != entry.getValue().contains(pageId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
