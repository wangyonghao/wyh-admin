package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.system.admin.app.dto.catalog.CatalogCommands;
import top.wyhao.system.admin.app.dto.catalog.CatalogViews;
import top.wyhao.system.admin.app.service.impl.MenuCatalogAppService;

import java.util.List;

/**
 * 菜单草稿与发布。
 */
@Tag(name = "菜单目录 API")
@RestController
@RequiredArgsConstructor
public class MenuCatalogController {

    private final MenuCatalogAppService menuCatalogAppService;

    @Operation(summary = "读取菜单草稿")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/system/menu/catalog")
    public CatalogViews.MenuWorkspace workspace(@RequestParam String scope) {
        return menuCatalogAppService.workspace(scope);
    }

    @Operation(summary = "保存菜单草稿")
    @SaCheckPermission("system:menu:update")
    @PutMapping("/system/menu/catalog")
    public CatalogViews.MenuWorkspace save(@RequestBody @Valid CatalogCommands.SaveMenu command) {
        return menuCatalogAppService.save(command);
    }

    @Operation(summary = "放弃菜单草稿")
    @SaCheckPermission("system:menu:update")
    @PostMapping("/system/menu/catalog/discard")
    public CatalogViews.MenuWorkspace discard(@RequestBody @Valid CatalogCommands.PublishMenu command) {
        return menuCatalogAppService.discard(command.getScope(), command.getVersion());
    }

    @Operation(summary = "发布菜单草稿")
    @SaCheckPermission("system:menu:update")
    @PostMapping("/system/menu/catalog/publish")
    public CatalogViews.MenuPublish publish(@RequestBody @Valid CatalogCommands.PublishMenu command) {
        return menuCatalogAppService.publish(command);
    }

    @Operation(summary = "菜单发布记录")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/system/menu/catalog/releases")
    public List<CatalogViews.MenuRelease> releases(@RequestParam String scope) {
        return menuCatalogAppService.releases(scope);
    }

    @Operation(summary = "把历史发布放回草稿")
    @SaCheckPermission("system:menu:update")
    @PostMapping("/system/menu/catalog/rollback")
    public CatalogViews.MenuWorkspace rollback(@RequestBody @Valid CatalogCommands.PublishMenu command) {
        return menuCatalogAppService.rollback(command.getScope(), command.getVersion(), command.getReleaseId());
    }

    @Operation(summary = "按角色查看已发布菜单")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/system/menu/catalog/roles")
    public List<CatalogViews.RoleMenus> roles(@RequestParam String scope) {
        return menuCatalogAppService.roles(scope);
    }
}
