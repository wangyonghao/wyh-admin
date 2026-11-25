package top.wyhao.system.admin.app.dto.catalog;

import lombok.Data;
import top.wyhao.system.admin.domain.catalog.CatalogNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CatalogViews {

    private CatalogViews() {
    }

    @Data
    public static class MenuWorkspace {
        private String scope;
        private Integer version;
        private boolean dirty;
        private List<CatalogNode> nodes = new ArrayList<>();
    }

    @Data
    public static class MenuPublish {
        private boolean confirmRequired;
        private boolean applied;
        private List<String> changes = new ArrayList<>();
        private List<String> affectedRoleNames = new ArrayList<>();
        private MenuWorkspace workspace;
    }

    @Data
    public static class MenuRelease {
        private String id;
        private Integer version;
        private LocalDateTime createTime;
    }

    @Data
    public static class RoleMenus {
        private String id;
        private String name;
        private List<String> menuIds = new ArrayList<>();
    }

    @Data
    public static class MatrixMenu {
        private String id;
        private String parentId;
        private String name;
        private Integer type;
        private boolean required;
        private boolean enabled;
    }

    @Data
    public static class MatrixPackage {
        private String id;
        private String name;
        private String kind;
        private String lifecycle;
        private Integer version;
        private long tenantCount;
        private boolean dirty;
        private boolean everPublished;
        private List<String> menuIds = new ArrayList<>();
    }

    @Data
    public static class Matrix {
        private List<MatrixMenu> menus = new ArrayList<>();
        private List<MatrixPackage> packages = new ArrayList<>();
    }

    @Data
    public static class PackageMenus {
        private String id;
        private Integer version;
        private boolean dirty;
        private List<String> menuIds = new ArrayList<>();
    }
}
