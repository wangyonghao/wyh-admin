package top.wyhao.system.admin.domain.catalog;

/**
 * 套餐可选的已发布菜单。目录不入库，按钮跟随页面。
 */
public record PackageMenuRef(String id, String parentId, int type, boolean required) {
}
