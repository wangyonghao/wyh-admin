package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.system.admin.app.dto.catalog.CatalogCommands;
import top.wyhao.system.admin.app.dto.catalog.CatalogViews;
import top.wyhao.system.admin.app.service.impl.PackageCatalogAppService;

/**
 * 套餐矩阵。
 */
@Tag(name = "套餐目录 API")
@RestController
@RequiredArgsConstructor
public class PackageCatalogController {

    private final PackageCatalogAppService packageCatalogAppService;

    @Operation(summary = "套餐矩阵")
    @SaCheckPermission("tenant:package:list")
    @GetMapping("/tenant/package/matrix")
    public CatalogViews.Matrix matrix() {
        return packageCatalogAppService.matrix();
    }

    @Operation(summary = "保存套餐菜单草稿")
    @SaCheckPermission("tenant:package:update")
    @PutMapping("/tenant/package/{id}/menus")
    public CatalogViews.PackageMenus saveMenus(@PathVariable Long id, @RequestBody @Valid CatalogCommands.SavePackageMenus command) {
        return packageCatalogAppService.saveMenus(id, command);
    }

    @Operation(summary = "发布套餐")
    @SaCheckPermission("tenant:package:update")
    @PostMapping("/tenant/package/{id}/publish")
    public CatalogViews.PackageMenus publish(@PathVariable Long id, @RequestBody @Valid CatalogCommands.SavePackageMenus command) {
        return packageCatalogAppService.publish(id, command.getVersion());
    }

    @Operation(summary = "放弃套餐菜单变更")
    @SaCheckPermission("tenant:package:update")
    @PostMapping("/tenant/package/{id}/discard")
    public CatalogViews.PackageMenus discard(@PathVariable Long id, @RequestBody @Valid CatalogCommands.SavePackageMenus command) {
        return packageCatalogAppService.discard(id, command.getVersion());
    }

    @Operation(summary = "下架套餐")
    @SaCheckPermission("tenant:package:update")
    @PostMapping("/tenant/package/{id}/delist")
    public void delist(@PathVariable Long id, @RequestBody @Valid CatalogCommands.SavePackageMenus command) {
        packageCatalogAppService.delist(id, command.getVersion());
    }

    @Operation(summary = "重新上架套餐")
    @SaCheckPermission("tenant:package:update")
    @PostMapping("/tenant/package/{id}/relist")
    public void relist(@PathVariable Long id, @RequestBody @Valid CatalogCommands.SavePackageMenus command) {
        packageCatalogAppService.relist(id, command.getVersion());
    }

    @Operation(summary = "复制为草稿套餐")
    @SaCheckPermission("tenant:package:create")
    @PostMapping("/tenant/package/{id}/copy")
    public CatalogViews.MatrixPackage copy(@PathVariable Long id, @RequestBody @Valid CatalogCommands.NamePackage command) {
        return packageCatalogAppService.copy(id, command);
    }

    @Operation(summary = "新建套餐")
    @SaCheckPermission("tenant:package:create")
    @PostMapping("/tenant/package/catalog")
    public CatalogViews.MatrixPackage create(@RequestBody @Valid CatalogCommands.NamePackage command) {
        return packageCatalogAppService.create(command);
    }

    @Operation(summary = "修改未发布套餐名称")
    @SaCheckPermission("tenant:package:update")
    @PatchMapping("/tenant/package/{id}/name")
    public void rename(@PathVariable Long id, @RequestBody @Valid CatalogCommands.NamePackage command) {
        packageCatalogAppService.rename(id, command.getVersion() == null ? 0 : command.getVersion(), command.getName());
    }

    @Operation(summary = "删除未发布套餐")
    @SaCheckPermission("tenant:package:delete")
    @DeleteMapping("/tenant/package/catalog/{id}")
    public void delete(@PathVariable Long id) {
        packageCatalogAppService.delete(id);
    }
}
