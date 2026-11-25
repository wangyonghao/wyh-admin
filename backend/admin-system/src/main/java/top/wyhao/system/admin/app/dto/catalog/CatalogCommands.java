package top.wyhao.system.admin.app.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.wyhao.system.admin.domain.catalog.CatalogNode;

import java.util.ArrayList;
import java.util.List;

public final class CatalogCommands {

    private CatalogCommands() {
    }

    @Data
    public static class SaveMenu {
        @NotBlank
        private String scope;
        @NotNull
        private Integer version;
        @NotNull
        private List<CatalogNode> nodes = new ArrayList<>();
    }

    @Data
    public static class PublishMenu {
        @NotBlank
        private String scope;
        @NotNull
        private Integer version;
        private boolean confirm;
        private boolean dryRun;
        private Long releaseId;
    }

    @Data
    public static class SavePackageMenus {
        @NotNull
        private Integer version;
        @NotNull
        private List<String> menuIds = new ArrayList<>();
    }

    @Data
    public static class NamePackage {
        @NotBlank
        private String name;
        private String kind;
        private Integer version;
    }
}
