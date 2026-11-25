package top.wyhao.system.admin.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.cmn.core.constant.CacheConstants;
import top.wyhao.cmn.core.constant.StringConstants;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.system.admin.app.dto.catalog.CatalogCommands;
import top.wyhao.system.admin.app.dto.catalog.CatalogViews;
import top.wyhao.system.admin.domain.catalog.CatalogNode;
import top.wyhao.system.admin.domain.catalog.MenuCatalogRules;
import top.wyhao.system.admin.domain.catalog.MenuUsage;
import top.wyhao.system.admin.domain.catalog.PublishAssessment;
import top.wyhao.system.admin.domain.exception.MenuException;
import top.wyhao.system.admin.domain.model.MenuType;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.MenuCatalogDraftDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.MenuCatalogReleaseDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysMenuDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleMenuDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageMenuDO;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageMenuDraftDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.MenuCatalogDraftMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.MenuCatalogReleaseMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysMenuMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysRoleMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysRoleMenuMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageGrantMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMenuDraftMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMenuMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 平台菜单和租户菜单的草稿、发布与回滚。线上导航只读已发布数据。
 */
@Service
@RequiredArgsConstructor
public class MenuCatalogAppService {

    private static final TypeReference<List<CatalogNode>> NODE_LIST = new TypeReference<>() {
    };

    private final MenuCatalogDraftMapper draftMapper;
    private final MenuCatalogReleaseMapper releaseMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final TenantPackageMapper packageMapper;
    private final TenantPackageMenuMapper packageMenuMapper;
    private final TenantPackageMenuDraftMapper packageMenuDraftMapper;
    private final TenantPackageGrantMapper grantMapper;
    private final ObjectMapper objectMapper;
    private final CatalogAuditWriter auditWriter;

    public CatalogViews.MenuWorkspace workspace(String scope) {
        scope = normalize(scope);
        ensure(scope);
        return view(scope);
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.MenuWorkspace save(CatalogCommands.SaveMenu command) {
        String scope = normalize(command.getScope());
        MenuCatalogDraftDO row = require(scope, command.getVersion());
        List<CatalogNode> next = normalizeDraft(scope, command.getNodes());
        store(row, next, command.getVersion() + 1);
        auditWriter.record("menu", "save", scope, scope, String.valueOf(command.getVersion()), String.valueOf(command.getVersion() + 1));
        return view(scope);
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.MenuWorkspace discard(String scope, int version) {
        scope = normalize(scope);
        MenuCatalogDraftDO row = require(scope, version);
        List<CatalogNode> published = publishedNodes(scope);
        store(row, published, version + 1);
        auditWriter.record("menu", "discard", scope, scope, null, null);
        return view(scope);
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.MenuPublish publish(CatalogCommands.PublishMenu command) {
        String scope = normalize(command.getScope());
        MenuCatalogDraftDO row = require(scope, command.getVersion());
        List<CatalogNode> published = publishedNodes(scope);
        List<CatalogNode> draft = read(row.getDocument());
        PublishAssessment assessment = MenuCatalogRules.assess(draft, published, retiredNodes(scope), usage(scope, draft));
        CatalogViews.MenuPublish result = new CatalogViews.MenuPublish();
        result.setChanges(assessment.changes());
        result.setAffectedRoleNames(assessment.affectedRoleNames());
        result.setConfirmRequired(assessment.confirmRequired());
        if (command.isDryRun() || (assessment.confirmRequired() && !command.isConfirm())) {
            result.setWorkspace(view(scope));
            return result;
        }
        apply(scope, draft, assessment);
        List<CatalogNode> fresh = publishedNodes(scope);
        store(row, fresh, command.getVersion() + 1);
        rememberRelease(scope, command.getVersion() + 1, fresh);
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
        auditWriter.record("menu", "publish", scope, scope, null, String.join("\n", assessment.changes()));
        result.setApplied(true);
        result.setConfirmRequired(false);
        result.setWorkspace(view(scope));
        return result;
    }

    public List<CatalogViews.MenuRelease> releases(String scope) {
        scope = normalize(scope);
        return releaseMapper.lambdaQuery()
                .eq(MenuCatalogReleaseDO::getScope, scope)
                .orderByDesc(MenuCatalogReleaseDO::getCreateTime)
                .last("LIMIT 20")
                .list()
                .stream()
                .map(row -> {
                    CatalogViews.MenuRelease view = new CatalogViews.MenuRelease();
                    view.setId(String.valueOf(row.getId()));
                    view.setVersion(row.getVersion());
                    view.setCreateTime(row.getCreateTime());
                    return view;
                })
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public CatalogViews.MenuWorkspace rollback(String scope, int version, Long releaseId) {
        scope = normalize(scope);
        MenuCatalogReleaseDO release = releaseMapper.selectById(releaseId);
        if (release == null || !scope.equals(release.getScope())) {
            throw MenuException.notFound();
        }
        MenuCatalogDraftDO row = require(scope, version);
        List<CatalogNode> draft = MenuCatalogRules.reindex(
                MenuCatalogRules.rollbackDraft(publishedNodes(scope), read(release.getDocument())));
        MenuCatalogRules.assertStructure(draft);
        store(row, draft, version + 1);
        auditWriter.record("menu", "rollback", scope, scope, String.valueOf(release.getVersion()), null);
        return view(scope);
    }

    public List<CatalogViews.RoleMenus> roles(String scope) {
        scope = normalize(scope);
        Set<String> menuIds = publishedNodes(scope).stream().map(CatalogNode::getId).collect(Collectors.toSet());
        Map<Long, List<String>> byRole = new HashMap<>();
        for (SysRoleMenuDO link : roleMenuMapper.selectList(null)) {
            String menuId = String.valueOf(link.getMenuId());
            if (menuIds.contains(menuId)) {
                byRole.computeIfAbsent(link.getRoleId(), key -> new ArrayList<>()).add(menuId);
            }
        }
        List<CatalogViews.RoleMenus> result = new ArrayList<>();
        for (SysRoleDO role : roleMapper.selectList(null)) {
            CatalogViews.RoleMenus item = new CatalogViews.RoleMenus();
            item.setId(String.valueOf(role.getId()));
            item.setName(role.getName());
            item.setMenuIds(byRole.getOrDefault(role.getId(), List.of()));
            result.add(item);
        }
        result.sort(Comparator.comparing(CatalogViews.RoleMenus::getName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    private List<CatalogNode> normalizeDraft(String scope, List<CatalogNode> nodes) {
        List<CatalogNode> published = publishedNodes(scope);
        List<CatalogNode> retired = retiredNodes(scope);
        List<CatalogNode> known = new ArrayList<>(published);
        known.addAll(retired);
        List<CatalogNode> next = MenuCatalogRules.reindex(MenuCatalogRules.prepare(MenuCatalogRules.seal(
                MenuCatalogRules.assignIds(nodes == null ? List.of() : nodes, this::nextId), known)));
        next = retainButtons(next, published);
        MenuCatalogRules.assertStructure(next);
        MenuCatalogRules.assess(next, published, retired, usage(scope, next));
        return next;
    }

    private List<CatalogNode> retainButtons(List<CatalogNode> draft, List<CatalogNode> published) {
        Set<String> ids = draft.stream().map(CatalogNode::getId).collect(Collectors.toSet());
        Map<String, CatalogNode> byId = new HashMap<>();
        for (CatalogNode node : draft) {
            byId.put(node.getId(), node);
        }
        List<CatalogNode> merged = new ArrayList<>(draft);
        for (CatalogNode old : published) {
            if (!Integer.valueOf(3).equals(old.getType()) || ids.contains(old.getId())) {
                continue;
            }
            CatalogNode parent = byId.get(old.parentKey());
            if (parent == null) {
                continue;
            }
            CatalogNode copy = old.copy();
            copy.setPendingDelete(parent.on(parent.getPendingDelete()));
            merged.add(copy);
        }
        return merged;
    }

    private void apply(String scope, List<CatalogNode> draft, PublishAssessment assessment) {
        Map<Long, SysMenuDO> existing = menuMapper.lambdaQuery()
                .eq(SysMenuDO::getScope, scope)
                .list()
                .stream()
                .collect(Collectors.toMap(SysMenuDO::getId, row -> row, (left, right) -> left));
        for (CatalogNode node : draft) {
            Long id = Long.valueOf(node.getId());
            if (node.on(node.getPendingDelete())) {
                if (existing.containsKey(id)) {
                    menuMapper.lambdaUpdate()
                            .eq(SysMenuDO::getId, id)
                            .set(SysMenuDO::getStatus, StatusEnum.DISABLE)
                            .set(SysMenuDO::getDeletedAt, LocalDateTime.now())
                            .update();
                }
                continue;
            }
            if (existing.containsKey(id)) {
                menuMapper.lambdaUpdate()
                        .eq(SysMenuDO::getId, id)
                        .set(SysMenuDO::getParentId, parentId(node))
                        .set(SysMenuDO::getType, typeOf(node.getType()))
                        .set(SysMenuDO::getName, node.getName())
                        .set(SysMenuDO::getPath, node.navigation() ? node.getPath() : null)
                        .set(SysMenuDO::getIcon, node.getIcon())
                        .set(SysMenuDO::getPermission, node.getPermission())
                        .set(SysMenuDO::getSort, node.getSort())
                        .set(SysMenuDO::getStatus, node.on(node.getEnabled()) ? StatusEnum.ENABLE : StatusEnum.DISABLE)
                        .set(SysMenuDO::getComponent, componentOf(node))
                        .set(SysMenuDO::getIsRequired, node.on(node.getRequired()))
                        .setSql("deleted_at = null")
                        .update();
            } else {
                SysMenuDO created = new SysMenuDO();
                created.setId(id);
                created.setParentId(parentId(node));
                created.setType(typeOf(node.getType()));
                created.setName(node.getName());
                created.setPath(node.navigation() ? node.getPath() : null);
                created.setIcon(node.getIcon());
                created.setPermission(node.getPermission());
                created.setSort(node.getSort());
                created.setStatus(node.on(node.getEnabled()) ? StatusEnum.ENABLE : StatusEnum.DISABLE);
                created.setComponent(componentOf(node));
                created.setCode(node.getCode());
                created.setScope(scope);
                created.setIsSystem(node.on(node.getSystem()));
                created.setIsRequired(node.on(node.getRequired()));
                created.setIsExternal(false);
                created.setIsCache(false);
                created.setIsHidden(false);
                menuMapper.insert(created);
            }
        }
        if (!assessment.softDeleteIds().isEmpty()) {
            List<Long> ids = assessment.softDeleteIds().stream().map(Long::valueOf).toList();
            roleMenuMapper.lambdaUpdate().in(SysRoleMenuDO::getMenuId, ids).remove();
            packageMenuMapper.lambdaUpdate().in(TenantPackageMenuDO::getMenuId, ids).remove();
            packageMenuDraftMapper.lambdaUpdate().in(TenantPackageMenuDraftDO::getMenuId, ids).remove();
        }
    }

    private MenuUsage usage(String scope, List<CatalogNode> draft) {
        Set<String> ids = draft.stream()
                .filter(node -> node.on(node.getPendingDelete()) || !node.on(node.getEnabled()))
                .map(CatalogNode::getId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return MenuUsage.empty();
        }
        Map<String, List<String>> blocking = new HashMap<>();
        Map<String, List<String>> loose = new HashMap<>();
        Map<String, List<String>> using = new HashMap<>();
        if ("TENANT".equals(scope)) {
            Map<Long, TenantPackageDO> packages = packageMapper.selectList(null).stream()
                    .collect(Collectors.toMap(TenantPackageDO::getId, row -> row, (left, right) -> left));
            Map<Long, Long> tenants = new HashMap<>();
            grantMapper.selectList(null).forEach(grant -> tenants.merge(grant.getPackageId(), 1L, Long::sum));
            collectPackages(packages, tenants, packageMenuMapper.selectList(null).stream()
                    .map(row -> Map.entry(row.getPackageId(), row.getMenuId())).toList(), true, ids, blocking, loose, using);
            collectPackages(packages, tenants, packageMenuDraftMapper.selectList(null).stream()
                    .map(row -> Map.entry(row.getPackageId(), row.getMenuId())).toList(), false, ids, blocking, loose, using);
        }
        Map<String, List<String>> roles = new HashMap<>();
        List<Long> menuIds = ids.stream().map(Long::valueOf).toList();
        List<SysRoleMenuDO> links = roleMenuMapper.lambdaQuery().in(SysRoleMenuDO::getMenuId, menuIds).list();
        Map<Long, String> roleNames = roleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysRoleDO::getId, SysRoleDO::getName, (left, right) -> left));
        for (SysRoleMenuDO link : links) {
            roles.computeIfAbsent(String.valueOf(link.getMenuId()), key -> new ArrayList<>())
                    .add(roleNames.getOrDefault(link.getRoleId(), String.valueOf(link.getRoleId())));
        }
        MenuUsage usage = new MenuUsage(blocking, loose, roles, using);
        return "TENANT".equals(scope) ? usage : usage.withoutPackages();
    }

    private void collectPackages(Map<Long, TenantPackageDO> packages, Map<Long, Long> tenants,
                                 List<Map.Entry<Long, Long>> links, boolean live, Set<String> ids,
                                 Map<String, List<String>> blocking, Map<String, List<String>> loose,
                                 Map<String, List<String>> using) {
        for (Map.Entry<Long, Long> link : links) {
            String menuId = String.valueOf(link.getValue());
            if (!ids.contains(menuId)) {
                continue;
            }
            TenantPackageDO pkg = packages.get(link.getKey());
            if (pkg == null) {
                continue;
            }
            String lifecycle = pkg.getLifecycle() == null ? "PUBLISHED" : pkg.getLifecycle();
            long count = tenants.getOrDefault(pkg.getId(), 0L);
            boolean stock = "DELISTED".equals(lifecycle) && count > 0;
            if (live && ("PUBLISHED".equals(lifecycle) || stock)) {
                blocking.computeIfAbsent(menuId, key -> new ArrayList<>()).add(pkg.getName());
            } else if (!live && ("DRAFT".equals(lifecycle) || ("DELISTED".equals(lifecycle) && count == 0))) {
                loose.computeIfAbsent(menuId, key -> new ArrayList<>()).add(pkg.getName());
            }
            using.computeIfAbsent(menuId, key -> new ArrayList<>()).add(pkg.getName());
        }
    }

    private CatalogViews.MenuWorkspace view(String scope) {
        MenuCatalogDraftDO row = ensure(scope);
        List<CatalogNode> draft = read(row.getDocument());
        List<CatalogNode> published = publishedNodes(scope);
        CatalogViews.MenuWorkspace workspace = new CatalogViews.MenuWorkspace();
        workspace.setScope(scope);
        workspace.setVersion(row.getVersion());
        workspace.setNodes(draft);
        workspace.setDirty(!canonical(draft).equals(canonical(published)));
        return workspace;
    }

    private MenuCatalogDraftDO ensure(String scope) {
        MenuCatalogDraftDO row = draftMapper.selectById(scope);
        if (row != null) {
            return row;
        }
        row = new MenuCatalogDraftDO();
        row.setScope(scope);
        row.setVersion(1);
        row.setBaseVersion(1);
        row.setDocument(write(publishedNodes(scope)));
        row.setUpdateTime(LocalDateTime.now());
        row.setUpdateUser(currentUser());
        try {
            draftMapper.insert(row);
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            return draftMapper.selectById(scope);
        }
        return row;
    }

    private MenuCatalogDraftDO require(String scope, int version) {
        MenuCatalogDraftDO row = ensure(scope);
        if (!Objects.equals(row.getVersion(), version)) {
            throw MenuException.versionConflict();
        }
        return row;
    }

    private void store(MenuCatalogDraftDO row, List<CatalogNode> nodes, int version) {
        int updated = draftMapper.update(null, new LambdaUpdateWrapper<MenuCatalogDraftDO>()
                .eq(MenuCatalogDraftDO::getScope, row.getScope())
                .eq(MenuCatalogDraftDO::getVersion, row.getVersion())
                .set(MenuCatalogDraftDO::getDocument, write(nodes))
                .set(MenuCatalogDraftDO::getVersion, version)
                .set(MenuCatalogDraftDO::getBaseVersion, version)
                .set(MenuCatalogDraftDO::getUpdateUser, currentUser())
                .set(MenuCatalogDraftDO::getUpdateTime, LocalDateTime.now()));
        if (updated == 0) {
            throw MenuException.versionConflict();
        }
    }

    private void rememberRelease(String scope, int version, List<CatalogNode> nodes) {
        MenuCatalogReleaseDO release = new MenuCatalogReleaseDO();
        release.setId(nextId());
        release.setScope(scope);
        release.setVersion(version);
        release.setDocument(write(nodes));
        release.setCreateUser(currentUser());
        release.setCreateTime(LocalDateTime.now());
        releaseMapper.insert(release);
        List<MenuCatalogReleaseDO> extra = releaseMapper.lambdaQuery()
                .eq(MenuCatalogReleaseDO::getScope, scope)
                .orderByDesc(MenuCatalogReleaseDO::getCreateTime)
                .list();
        if (extra.size() > 20) {
            for (MenuCatalogReleaseDO old : extra.subList(20, extra.size())) {
                releaseMapper.deleteById(old.getId());
            }
        }
    }

    private List<CatalogNode> publishedNodes(String scope) {
        return MenuCatalogRules.reindex(toNodes(menuMapper.lambdaQuery()
                .eq(SysMenuDO::getScope, scope)
                .isNull(SysMenuDO::getDeletedAt)
                .list()));
    }

    private List<CatalogNode> toNodes(List<SysMenuDO> rows) {
        List<CatalogNode> nodes = new ArrayList<>();
        for (SysMenuDO row : rows) {
            CatalogNode node = new CatalogNode();
            node.setId(String.valueOf(row.getId()));
            node.setParentId(row.getParentId() == null || row.getParentId() == 0L ? "0" : String.valueOf(row.getParentId()));
            node.setCode(row.getCode() == null ? "m" + row.getId() : row.getCode());
            node.setType(row.getType() == null ? 2 : row.getType().getValue());
            node.setName(row.getName());
            node.setIcon(row.getIcon());
            node.setPath(row.getPath());
            node.setPermission(row.getPermission());
            node.setSort(row.getSort() == null ? 0 : row.getSort());
            node.setEnabled(row.getStatus() == null || StatusEnum.ENABLE.equals(row.getStatus()));
            node.setSystem(Boolean.TRUE.equals(row.getIsSystem()));
            node.setRequired(Boolean.TRUE.equals(row.getIsRequired()));
            node.setPublished(true);
            node.setPendingDelete(false);
            nodes.add(node);
        }
        return nodes;
    }

    private List<CatalogNode> retiredNodes(String scope) {
        return toNodes(menuMapper.lambdaQuery()
                .eq(SysMenuDO::getScope, scope)
                .isNotNull(SysMenuDO::getDeletedAt)
                .list());
    }

    private String canonical(List<CatalogNode> nodes) {
        return nodes.stream()
                .filter(node -> !node.on(node.getPendingDelete()))
                .sorted(Comparator.comparing(CatalogNode::getId, Comparator.nullsLast(String::compareTo)))
                .map(node -> String.join("|",
                        node.getId(),
                        node.parentKey(),
                        String.valueOf(node.getType()),
                        node.getName(),
                        node.getIcon() == null ? "" : node.getIcon(),
                        node.getPath() == null ? "" : node.getPath(),
                        node.getPermission() == null ? "" : node.getPermission(),
                        String.valueOf(node.getSort()),
                        String.valueOf(node.on(node.getEnabled())),
                        String.valueOf(node.on(node.getRequired()))))
                .collect(Collectors.joining("\n"));
    }

    private List<CatalogNode> read(String document) {
        try {
            List<CatalogNode> nodes = objectMapper.readValue(document, NODE_LIST);
            return nodes == null ? List.of() : nodes;
        } catch (JsonProcessingException ex) {
            throw MenuException.of("MENU_DRAFT_INVALID", "菜单草稿无法读取");
        }
    }

    private String write(List<CatalogNode> nodes) {
        try {
            return objectMapper.writeValueAsString(nodes);
        } catch (JsonProcessingException ex) {
            throw MenuException.of("MENU_DRAFT_INVALID", "菜单草稿无法保存");
        }
    }

    private static String normalize(String scope) {
        if (!"PLATFORM".equals(scope) && !"TENANT".equals(scope)) {
            throw MenuException.of("MENU_SCOPE_INVALID", "菜单范围不正确");
        }
        return scope;
    }

    private static long parentId(CatalogNode node) {
        String parent = node.parentKey();
        return "0".equals(parent) ? 0L : Long.parseLong(parent);
    }

    private static MenuType typeOf(Integer type) {
        if (Integer.valueOf(1).equals(type)) {
            return MenuType.DIR;
        }
        if (Integer.valueOf(3).equals(type)) {
            return MenuType.BUTTON;
        }
        return MenuType.MENU;
    }

    private static String componentOf(CatalogNode node) {
        if (Integer.valueOf(1).equals(node.getType())) {
            return "Layout";
        }
        if (!Integer.valueOf(2).equals(node.getType()) || node.getPath() == null) {
            return null;
        }
        String path = node.getPath().trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.isBlank()) {
            return null;
        }
        return path.endsWith("/index") ? path : path + "/index";
    }

    private long nextId() {
        return DefaultIdGeneratorProvider.INSTANCE.getShare().generate();
    }

    private Long currentUser() {
        try {
            return LoginUtil.getUserId();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
