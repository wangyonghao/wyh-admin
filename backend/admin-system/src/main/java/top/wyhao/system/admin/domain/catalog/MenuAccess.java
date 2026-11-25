package top.wyhao.system.admin.domain.catalog;

import top.wyhao.system.admin.domain.model.Menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录后可见菜单。平台成员看平台菜单；企业成员看租户菜单与有效套餐的交集。
 * 目录本身不授权，只要有可见子菜单就保留。
 */
public final class MenuAccess {

    private MenuAccess() {
    }

    public static List<Menu> platform(List<Menu> menus) {
        List<Menu> visible = new ArrayList<>();
        for (Menu menu : menus) {
            if (menu.getScope() == null || menu.getScope().isBlank() || "PLATFORM".equals(menu.getScope())) {
                visible.add(menu);
            }
        }
        return visible;
    }

    public static List<Menu> tenant(List<Menu> menus, Set<Long> allowedIds) {
        Map<Long, Menu> byId = new HashMap<>();
        for (Menu menu : menus) {
            if (menu.getId() != null) {
                byId.put(menu.getId(), menu);
            }
        }
        Set<Long> keep = new HashSet<>();
        for (Long allowedId : allowedIds) {
            Long current = allowedId;
            Set<Long> guard = new HashSet<>();
            while (current != null && current != 0L && guard.add(current)) {
                Menu node = byId.get(current);
                if (node == null || !"TENANT".equals(node.getScope())) {
                    break;
                }
                keep.add(current);
                current = node.getParentId();
            }
        }
        List<Menu> visible = new ArrayList<>();
        for (Menu menu : menus) {
            if (keep.contains(menu.getId())) {
                visible.add(menu);
            }
        }
        return visible;
    }
}
