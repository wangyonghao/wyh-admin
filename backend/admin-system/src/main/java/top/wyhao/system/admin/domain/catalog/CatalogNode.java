package top.wyhao.system.admin.domain.catalog;

import lombok.Data;

/**
 * 菜单草稿里的一个节点。标识在发布前可能是临时 id。
 */
@Data
public class CatalogNode {

    private String id;

    private String code;

    private String parentId;

    private Integer type;

    private String name;

    private String icon;

    private String path;

    private String permission;

    private Integer sort;

    private Boolean enabled;

    private Boolean system;

    private Boolean required;

    private Boolean published;

    private Boolean pendingDelete;

    public CatalogNode copy() {
        CatalogNode node = new CatalogNode();
        node.id = id;
        node.code = code;
        node.parentId = parentId;
        node.type = type;
        node.name = name;
        node.icon = icon;
        node.path = path;
        node.permission = permission;
        node.sort = sort;
        node.enabled = enabled;
        node.system = system;
        node.required = required;
        node.published = published;
        node.pendingDelete = pendingDelete;
        return node;
    }

    public boolean on(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public boolean navigation() {
        return type != null && type != 3;
    }

    public String parentKey() {
        if (parentId == null || parentId.isBlank() || "0".equals(parentId)) {
            return "0";
        }
        return parentId;
    }
}
