package top.wyhao.system.admin.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.system.admin.app.dto.catalog.CatalogCommands;
import top.wyhao.system.admin.app.dto.catalog.CatalogViews;
import top.wyhao.system.admin.domain.PackageMenuBinding;
import top.wyhao.system.admin.domain.catalog.CatalogNode;
import top.wyhao.system.admin.domain.catalog.PackageCatalogRules;
import top.wyhao.system.admin.domain.catalog.PackageMenuRef;
import top.wyhao.system.admin.domain.exception.PackageException;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMenuDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageMenuDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageMenuDraftDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysMenuMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysTenantMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageGrantMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMenuDraftMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMenuMapper;
import top.wyhao.system.admin.app.service.PackageMenuService;
import top.wyhao.system.admin.app.service.TenantService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 套餐矩阵：草稿勾选、按单个套餐发布、下架和重新上架。
 */
@Service
@RequiredArgsConstructor
public class PackageCatalogAppService {

    private final TenantPackageMapper packageMapper;
    private final TenantPackageMenuMapper packageMenuMapper;
    private final TenantPackageMenuDraftMapper draftMapper;
    private final TenantPackageGrantMapper grantMapper;
    private final SysTenantMapper tenantMapper;
    private final SysMenuMapper menuMapper;
    private final PackageMenuService packageMenuService;
    private final TenantService tenantService;
    private final CatalogAuditWriter auditWriter;

    public CatalogViews.Matrix matrix() {
        List<CatalogNode> menus = tenantMenus();
        Map<Long, Set<String>> drafts = grouped(true);
        Map<Long, Set<String>> live = grouped(false);
        Map<Long, Long> tenants = tenantCounts();
        CatalogViews.Matrix matrix = new CatalogViews.Matrix();
        for (CatalogNode menu : menus) {
            if (Integer.valueOf(3).equals(menu.getType())) {
                continue;
            }
            CatalogViews.MatrixMenu row = new CatalogViews.MatrixMenu();
            row.setId(menu.getId());
            row.setParentId(menu.parentKey());
            row.setName(menu.getName());
            row.setType(menu.getType());
            row.setRequired(menu.on(menu.getRequired()));
            row.setEnabled(menu.on(menu.getEnabled()));
            matrix.getMenus().add(row);
        }
        List<TenantPackageDO> packages = packageMapper.lambdaQuery()
                .orderByAsc(TenantPackageDO::getSort)
                .orderByAsc(TenantPackageDO::getId)
                .list();
        for (TenantPackageDO pkg : packages) {
            CatalogViews.MatrixPackage column = column(pkg, drafts.getOrDefault(pkg.getId(), Set.of()), live.getOrDefault(pkg.getId(), Set.of()), tenants.getOrDefault(pkg.getId(), 0L));
            matrix.getPackages().add(column);
        }
        return matrix;
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.PackageMenus saveMenus(Long id, CatalogCommands.SavePackageMenus command) {
        TenantPackageDO pkg = require(id);
        checkVersion(pkg, command.getVersion());
        PackageCatalogRules.assertEditMenus(lifecycle(pkg));
        Set<String> selected = PackageCatalogRules.normalizeSelection(new HashSet<>(command.getMenuIds()), refs());
        replaceDraft(id, selected);
        bump(pkg, command.getVersion() + 1);
        auditWriter.record("package", "menus", String.valueOf(id), pkg.getName(), null, String.join(",", selected));
        return menusView(require(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.PackageMenus publish(Long id, int version) {
        TenantPackageDO pkg = require(id);
        checkVersion(pkg, version);
        PackageCatalogRules.assertPublish(lifecycle(pkg));
        Set<String> selected = PackageCatalogRules.normalizeSelection(draftIds(id), refs());
        List<Long> menuIds = selected.stream().map(Long::valueOf).toList();
        packageMenuService.add(menuIds, id);
        replaceDraft(id, selected);
        pkg.setLifecycle("PUBLISHED");
        pkg.setStatus(StatusEnum.ENABLE);
        if (pkg.getPublishedAt() == null) {
            pkg.setPublishedAt(LocalDateTime.now());
        }
        pkg.setVersion(version + 1);
        packageMapper.updateById(pkg);
        tenantService.updateTenantMenu(PackageMenuBinding.menusForTenantAdmin(menuIds), id);
        auditWriter.record("package", "publish", String.valueOf(id), pkg.getName(), null, "发布后名称不可修改");
        return menusView(require(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.PackageMenus discard(Long id, int version) {
        TenantPackageDO pkg = require(id);
        checkVersion(pkg, version);
        replaceDraft(id, liveIds(id));
        bump(pkg, version + 1);
        auditWriter.record("package", "discard", String.valueOf(id), pkg.getName(), null, null);
        return menusView(require(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delist(Long id, int version) {
        TenantPackageDO pkg = require(id);
        checkVersion(pkg, version);
        PackageCatalogRules.assertDelist(lifecycle(pkg), dirty(id));
        pkg.setLifecycle("DELISTED");
        pkg.setStatus(StatusEnum.DISABLE);
        pkg.setDelistedAt(LocalDateTime.now());
        pkg.setVersion(version + 1);
        packageMapper.updateById(pkg);
        auditWriter.record("package", "delist", String.valueOf(id), pkg.getName(), null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void relist(Long id, int version) {
        TenantPackageDO pkg = require(id);
        checkVersion(pkg, version);
        PackageCatalogRules.assertRelist(lifecycle(pkg));
        boolean updated = packageMapper.lambdaUpdate()
                .eq(TenantPackageDO::getId, id)
                .eq(TenantPackageDO::getVersion, version)
                .set(TenantPackageDO::getLifecycle, "PUBLISHED")
                .set(TenantPackageDO::getStatus, StatusEnum.ENABLE)
                .set(TenantPackageDO::getVersion, version + 1)
                .setSql("delisted_at = null")
                .update();
        if (!updated) {
            throw PackageException.versionConflict();
        }
        auditWriter.record("package", "relist", String.valueOf(id), pkg.getName(), null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.MatrixPackage copy(Long id, CatalogCommands.NamePackage command) {
        TenantPackageDO source = require(id);
        return createPackage(command.getName(), command.getKind() == null ? source.getKind() : command.getKind(), draftIds(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.MatrixPackage create(CatalogCommands.NamePackage command) {
        return createPackage(command.getName(), command.getKind(), Set.of());
    }

    @Transactional(rollbackFor = Exception.class)
    public void rename(Long id, int version, String name) {
        TenantPackageDO pkg = require(id);
        checkVersion(pkg, version);
        PackageCatalogRules.assertRename(pkg.getPublishedAt() != null);
        checkName(name, id);
        pkg.setName(name.trim());
        pkg.setVersion(version + 1);
        packageMapper.updateById(pkg);
        auditWriter.record("package", "rename", String.valueOf(id), pkg.getName(), null, name.trim());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TenantPackageDO pkg = require(id);
        PackageCatalogRules.assertDelete(lifecycle(pkg), pkg.getPublishedAt() != null);
        if (tenantMapper.lambdaQuery().eq(TenantDO::getPackageId, id).exists()
                || grantMapper.lambdaQuery().eq(top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageGrantDO::getPackageId, id).exists()) {
            throw PackageException.inUse();
        }
        packageMenuService.add(List.of(), id);
        draftMapper.lambdaUpdate().eq(TenantPackageMenuDraftDO::getPackageId, id).remove();
        packageMapper.deleteById(id);
        auditWriter.record("package", "delete", String.valueOf(id), pkg.getName(), null, null);
    }

    private CatalogViews.MatrixPackage createPackage(String name, String kind, Set<String> menuIds) {
        checkName(name, null);
        String normalizedKind = "ADDON".equals(kind) ? "ADDON" : "MAIN";
        TenantPackageDO created = new TenantPackageDO();
        created.setName(name.trim());
        created.setKind(normalizedKind);
        created.setLifecycle("DRAFT");
        created.setStatus(StatusEnum.DISABLE);
        created.setVersion(1);
        created.setSort(999);
        created.setMenuCheckStrictly(true);
        packageMapper.insert(created);
        created.setCode("p" + created.getId());
        packageMapper.updateById(created);
        Set<String> selected = PackageCatalogRules.normalizeSelection(menuIds, refs());
        replaceDraft(created.getId(), selected);
        auditWriter.record("package", "create", String.valueOf(created.getId()), created.getName(), null, normalizedKind);
        TenantPackageDO stored = require(created.getId());
        return column(stored, selected, Set.of(), 0);
    }

    private void replaceDraft(Long packageId, Set<String> menuIds) {
        draftMapper.lambdaUpdate().eq(TenantPackageMenuDraftDO::getPackageId, packageId).remove();
        for (String menuId : menuIds) {
            draftMapper.insert(new TenantPackageMenuDraftDO(packageId, Long.valueOf(menuId)));
        }
    }

    private CatalogViews.PackageMenus menusView(TenantPackageDO pkg) {
        Set<String> draft = draftIds(pkg.getId());
        Set<String> live = liveIds(pkg.getId());
        CatalogViews.PackageMenus view = new CatalogViews.PackageMenus();
        view.setId(String.valueOf(pkg.getId()));
        view.setVersion(pkg.getVersion());
        view.setDirty(!draft.equals(live));
        view.setMenuIds(new ArrayList<>(draft));
        return view;
    }

    private CatalogViews.MatrixPackage column(TenantPackageDO pkg, Set<String> draft, Set<String> live, long tenants) {
        CatalogViews.MatrixPackage column = new CatalogViews.MatrixPackage();
        column.setId(String.valueOf(pkg.getId()));
        column.setName(pkg.getName());
        column.setKind(pkg.getKind() == null ? "MAIN" : pkg.getKind());
        column.setLifecycle(lifecycle(pkg));
        column.setVersion(pkg.getVersion() == null ? 1 : pkg.getVersion());
        column.setTenantCount(tenants);
        column.setDirty(!draft.equals(live));
        column.setEverPublished(pkg.getPublishedAt() != null);
        column.setMenuIds(new ArrayList<>(draft));
        return column;
    }

    private List<PackageMenuRef> refs() {
        List<PackageMenuRef> refs = new ArrayList<>();
        for (CatalogNode menu : tenantMenus()) {
            refs.add(new PackageMenuRef(menu.getId(), menu.parentKey(), menu.getType(), menu.on(menu.getRequired())));
        }
        return refs;
    }

    private List<CatalogNode> tenantMenus() {
        List<SysMenuDO> rows = menuMapper.lambdaQuery()
                .eq(SysMenuDO::getScope, "TENANT")
                .isNull(SysMenuDO::getDeletedAt)
                .orderByAsc(SysMenuDO::getSort)
                .list();
        List<CatalogNode> nodes = new ArrayList<>();
        for (SysMenuDO row : rows) {
            CatalogNode node = new CatalogNode();
            node.setId(String.valueOf(row.getId()));
            node.setParentId(row.getParentId() == null || row.getParentId() == 0L ? "0" : String.valueOf(row.getParentId()));
            node.setType(row.getType() == null ? 2 : row.getType().getValue());
            node.setName(row.getName());
            node.setRequired(Boolean.TRUE.equals(row.getIsRequired()));
            node.setEnabled(row.getStatus() == null || StatusEnum.ENABLE.equals(row.getStatus()));
            nodes.add(node);
        }
        nodes.sort(Comparator.comparing((CatalogNode node) -> node.parentKey()).thenComparing(node -> node.getSort() == null ? 0 : node.getSort()));
        return nodes;
    }

    private Map<Long, Set<String>> grouped(boolean draft) {
        Map<Long, Set<String>> grouped = new HashMap<>();
        if (draft) {
            for (TenantPackageMenuDraftDO row : draftMapper.selectList(null)) {
                grouped.computeIfAbsent(row.getPackageId(), key -> new HashSet<>()).add(String.valueOf(row.getMenuId()));
            }
        } else {
            for (TenantPackageMenuDO row : packageMenuMapper.selectList(null)) {
                grouped.computeIfAbsent(row.getPackageId(), key -> new HashSet<>()).add(String.valueOf(row.getMenuId()));
            }
        }
        return grouped;
    }

    private Map<Long, Long> tenantCounts() {
        Map<Long, Long> counts = new HashMap<>();
        grantMapper.selectList(null).forEach(grant -> counts.merge(grant.getPackageId(), 1L, Long::sum));
        return counts;
    }

    private Set<String> draftIds(Long packageId) {
        Set<String> ids = new HashSet<>();
        draftMapper.lambdaQuery()
                .eq(TenantPackageMenuDraftDO::getPackageId, packageId)
                .list()
                .forEach(row -> ids.add(String.valueOf(row.getMenuId())));
        return ids;
    }

    private Set<String> liveIds(Long packageId) {
        Set<String> ids = new HashSet<>();
        packageMenuMapper.lambdaQuery()
                .eq(TenantPackageMenuDO::getPackageId, packageId)
                .list()
                .forEach(row -> ids.add(String.valueOf(row.getMenuId())));
        return ids;
    }

    private boolean dirty(Long packageId) {
        return !draftIds(packageId).equals(liveIds(packageId));
    }

    private void bump(TenantPackageDO pkg, int version) {
        pkg.setVersion(version);
        packageMapper.updateById(pkg);
    }

    private void checkVersion(TenantPackageDO pkg, int version) {
        int current = pkg.getVersion() == null ? 1 : pkg.getVersion();
        if (current != version) {
            throw PackageException.versionConflict();
        }
    }

    private void checkName(String name, Long selfId) {
        if (name == null || name.isBlank()) {
            throw PackageException.of("PACKAGE_NAME_REQUIRED", "套餐名称不能为空");
        }
        boolean exists = packageMapper.lambdaQuery()
                .eq(TenantPackageDO::getName, name.trim())
                .ne(selfId != null, TenantPackageDO::getId, selfId)
                .exists();
        if (exists) {
            throw PackageException.nameExists(name.trim());
        }
    }

    private TenantPackageDO require(Long id) {
        TenantPackageDO pkg = packageMapper.selectById(id);
        if (pkg == null) {
            throw PackageException.notFound();
        }
        return pkg;
    }

    private static String lifecycle(TenantPackageDO pkg) {
        return pkg.getLifecycle() == null ? "PUBLISHED" : pkg.getLifecycle();
    }
}
