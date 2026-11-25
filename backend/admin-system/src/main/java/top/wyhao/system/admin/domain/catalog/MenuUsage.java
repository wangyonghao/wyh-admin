package top.wyhao.system.admin.domain.catalog;

import java.util.List;
import java.util.Map;

/**
 * 菜单被套餐和角色引用的情况。键是菜单 id。
 *
 * <p>blocking：已发布套餐，或仍有存量企业的已下架套餐。
 * loose：未发布套餐，或没有存量企业的已下架套餐。
 */
public record MenuUsage(
        Map<String, List<String>> blockingPackages,
        Map<String, List<String>> loosePackages,
        Map<String, List<String>> roles,
        Map<String, List<String>> packagesUsing) {

    public static MenuUsage empty() {
        return new MenuUsage(Map.of(), Map.of(), Map.of(), Map.of());
    }

    /** 套餐只约束租户菜单。平台菜单删除和停用只看角色。 */
    public MenuUsage withoutPackages() {
        return new MenuUsage(Map.of(), Map.of(), roles, Map.of());
    }
}
