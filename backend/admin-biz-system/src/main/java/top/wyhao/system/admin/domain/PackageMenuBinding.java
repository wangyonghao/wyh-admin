package top.wyhao.system.admin.domain;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 套餐菜单与租户管理员菜单的对应关系。
 *
 * <p>租户管理员得到的菜单集合就是套餐菜单，不增不减；比较时忽略顺序和重复项。
 */
public final class PackageMenuBinding {

    private PackageMenuBinding() {
    }

    public static boolean changed(Collection<Long> current, Collection<Long> requested) {
        return !normalize(current).equals(normalize(requested));
    }

    public static List<Long> menusForTenantAdmin(Collection<Long> packageMenuIds) {
        return List.copyOf(normalize(packageMenuIds));
    }

    private static Set<Long> normalize(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null) {
                unique.add(id);
            }
        }
        return unique;
    }
}
