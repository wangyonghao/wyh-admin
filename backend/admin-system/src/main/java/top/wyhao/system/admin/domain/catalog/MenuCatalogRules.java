package top.wyhao.system.admin.domain.catalog;

import top.wyhao.system.admin.domain.exception.MenuException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongSupplier;

/**
 * 菜单草稿的结构、保护和发布差异。调用方负责版本号和落库。
 */
public final class MenuCatalogRules {

    public static final int MAX_DEPTH = 3;

    private MenuCatalogRules() {
    }

    public static List<CatalogNode> assignIds(List<CatalogNode> nodes, LongSupplier ids) {
        Map<String, String> remap = new HashMap<>();
        for (CatalogNode node : nodes) {
            if (node.getId() == null || node.getId().isBlank() || node.getId().startsWith("tmp")) {
                remap.put(node.getId() == null ? "" : node.getId(), Long.toString(ids.getAsLong()));
            }
        }
        List<CatalogNode> assigned = new ArrayList<>();
        for (CatalogNode node : nodes) {
            CatalogNode copy = node.copy();
            String mapped = remap.get(copy.getId() == null ? "" : copy.getId());
            if (mapped != null) {
                copy.setId(mapped);
                copy.setCode("m" + mapped);
            }
            String parent = copy.parentKey();
            if (remap.containsKey(parent)) {
                copy.setParentId(remap.get(parent));
            }
            assigned.add(copy);
        }
        return assigned;
    }

    /**
     * 已发布节点的标识、类型、系统保护和必含标记以线上为准。目录不保留权限标识。
     */
    public static List<CatalogNode> seal(List<CatalogNode> draft, List<CatalogNode> published) {
        Map<String, CatalogNode> publishedById = index(published);
        List<CatalogNode> sealed = new ArrayList<>();
        for (CatalogNode node : draft) {
            CatalogNode copy = node.copy();
            CatalogNode old = publishedById.get(copy.getId());
            if (old == null) {
                copy.setSystem(false);
                copy.setPublished(false);
                if (copy.getCode() == null || copy.getCode().isBlank()) {
                    copy.setCode("m" + copy.getId());
                }
            } else {
                copy.setSystem(old.on(old.getSystem()));
                copy.setPublished(true);
                copy.setCode(old.getCode());
                copy.setType(old.getType());
                if (old.on(old.getRequired())) {
                    copy.setRequired(true);
                }
            }
            if (copy.getParentId() == null || copy.getParentId().isBlank()) {
                copy.setParentId("0");
            }
            if (copy.getEnabled() == null) {
                copy.setEnabled(true);
            }
            if (copy.getSort() == null) {
                copy.setSort(0);
            }
            if (copy.getType() == null) {
                copy.setType(2);
            }
            if (Integer.valueOf(1).equals(copy.getType())) {
                copy.setPermission(null);
            }
            if (copy.getPendingDelete() == null) {
                copy.setPendingDelete(false);
            }
            if (copy.getRequired() == null) {
                copy.setRequired(false);
            }
            sealed.add(copy);
        }
        return sealed;
    }

    /**
     * 删除页面时带上它的按钮。从未发布过的节点直接从草稿消失。
     */
    public static List<CatalogNode> prepare(List<CatalogNode> nodes) {
        List<CatalogNode> copy = nodes.stream().map(CatalogNode::copy).toList();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (CatalogNode node : copy) {
                if (!node.on(node.getPendingDelete()) || !node.navigation()) {
                    continue;
                }
                for (CatalogNode child : copy) {
                    if (node.getId().equals(child.parentKey())
                            && Integer.valueOf(3).equals(child.getType())
                            && !child.on(child.getPendingDelete())) {
                        child.setPendingDelete(true);
                        changed = true;
                    }
                }
            }
        }
        Set<String> removed = new HashSet<>();
        boolean dropping = true;
        while (dropping) {
            dropping = false;
            for (CatalogNode node : copy) {
                if (removed.contains(node.getId())) {
                    continue;
                }
                boolean dropSelf = node.on(node.getPendingDelete()) && !node.on(node.getPublished());
                boolean dropWithParent = removed.contains(node.parentKey());
                if (dropSelf || dropWithParent) {
                    removed.add(node.getId());
                    dropping = true;
                }
            }
        }
        List<CatalogNode> kept = new ArrayList<>();
        for (CatalogNode node : copy) {
            if (!removed.contains(node.getId())) {
                kept.add(node);
            }
        }
        return kept;
    }

    public static List<CatalogNode> reindex(List<CatalogNode> nodes) {
        Map<String, List<CatalogNode>> byParent = new HashMap<>();
        for (CatalogNode node : nodes) {
            byParent.computeIfAbsent(node.parentKey(), key -> new ArrayList<>()).add(node);
        }
        for (List<CatalogNode> siblings : byParent.values()) {
            siblings.sort(Comparator.comparing((CatalogNode node) -> node.getSort() == null ? 0 : node.getSort())
                    .thenComparing(CatalogNode::getId, Comparator.nullsLast(String::compareTo)));
            for (int index = 0; index < siblings.size(); index++) {
                siblings.get(index).setSort(index + 1);
            }
        }
        return nodes;
    }

    public static void assertStructure(List<CatalogNode> nodes) {
        Map<String, CatalogNode> byId = index(nodes);
        if (byId.size() != nodes.size()) {
            throw MenuException.of("MENU_ID_DUPLICATE", "菜单标识重复");
        }
        Set<String> codes = new HashSet<>();
        for (CatalogNode node : nodes) {
            if (node.getId() == null || node.getId().isBlank() || node.getId().startsWith("tmp")) {
                throw MenuException.of("MENU_ID_REQUIRED", "菜单标识尚未生成");
            }
            if (node.getCode() == null || node.getCode().isBlank()) {
                throw MenuException.of("MENU_CODE_REQUIRED", "菜单标识不能为空");
            }
            if (!codes.add(node.getCode())) {
                throw MenuException.of("MENU_CODE_REUSED", "菜单标识不能重复");
            }
            if (node.getName() == null || node.getName().isBlank()) {
                throw MenuException.of("MENU_NAME_REQUIRED", "菜单名称不能为空");
            }
            if (node.getType() == null || node.getType() < 1 || node.getType() > 3) {
                throw MenuException.of("MENU_TYPE_INVALID", "菜单类型不正确");
            }
            assertParent(node, byId);
            if (Integer.valueOf(2).equals(node.getType())
                    && !node.on(node.getPendingDelete())
                    && (node.getPath() == null || node.getPath().isBlank())) {
                throw MenuException.of("MENU_PATH_REQUIRED", "「" + node.getName() + "」需要路由地址");
            }
            if (node.navigation() && depth(node, byId) > MAX_DEPTH) {
                throw MenuException.of("MENU_DEPTH_INVALID", "「" + node.getName() + "」超过 " + MAX_DEPTH + " 级");
            }
        }
        Map<String, Set<String>> siblingNames = new HashMap<>();
        for (CatalogNode node : nodes) {
            if (!node.navigation() || node.on(node.getPendingDelete())) {
                continue;
            }
            Set<String> names = siblingNames.computeIfAbsent(node.parentKey(), key -> new HashSet<>());
            if (!names.add(node.getName())) {
                throw MenuException.titleExist(node.getName());
            }
        }
    }

    public static PublishAssessment assess(List<CatalogNode> draft, List<CatalogNode> published, List<CatalogNode> retired, MenuUsage usage) {
        Map<String, CatalogNode> publishedById = index(published);
        Map<String, CatalogNode> retiredById = index(retired == null ? List.of() : retired);
        Map<String, String> retiredCodeOwners = new HashMap<>();
        for (CatalogNode node : retiredById.values()) {
            if (node.getCode() != null && !node.getCode().isBlank()) {
                retiredCodeOwners.put(node.getCode(), node.getId());
            }
        }
        Set<String> draftIds = new HashSet<>();
        for (CatalogNode node : draft) {
            draftIds.add(node.getId());
        }
        for (CatalogNode old : published) {
            if (!draftIds.contains(old.getId())) {
                throw MenuException.of("MENU_DRAFT_INCOMPLETE", "草稿缺少已发布菜单「" + old.getName() + "」");
            }
        }
        List<String> changes = new ArrayList<>();
        List<String> softDeleteIds = new ArrayList<>();
        Set<String> unlinkMenuIds = new LinkedHashSet<>();
        Set<String> affectedRoles = new LinkedHashSet<>();
        for (CatalogNode node : draft) {
            CatalogNode old = publishedById.get(node.getId());
            CatalogNode prior = old != null ? old : retiredById.get(node.getId());
            String codeOwner = retiredCodeOwners.get(node.getCode());
            if (prior == null && codeOwner != null && !codeOwner.equals(node.getId())) {
                throw MenuException.of("MENU_CODE_REUSED", "菜单标识「" + node.getCode() + "」已使用过");
            }
            if (protectedNode(node) && (!node.on(node.getEnabled()) || node.on(node.getPendingDelete()))) {
                throw MenuException.of("MENU_PROTECTED", "「" + node.getName() + "」受保护，不能停用或删除");
            }
            if (node.on(node.getPendingDelete()) && node.navigation() && hasLiveNavChild(node, draft)) {
                throw MenuException.of("MENU_HAS_CHILDREN", "请先移走或删除「" + node.getName() + "」下的菜单");
            }
            if (prior == null) {
                if (!node.on(node.getPendingDelete())) {
                    changes.add("新增「" + node.getName() + "」");
                }
                continue;
            }
            if (old == null && !node.on(node.getPendingDelete())) {
                changes.add("恢复「" + node.getName() + "」");
            }
            if (node.on(node.getPendingDelete())) {
                List<String> blocking = usage.blockingPackages().getOrDefault(node.getId(), List.of());
                if (!blocking.isEmpty()) {
                    throw MenuException.of("MENU_PACKAGE_BLOCKED", "「" + node.getName() + "」仍被套餐引用：" + String.join("、", blocking));
                }
                if (!usage.loosePackages().getOrDefault(node.getId(), List.of()).isEmpty()) {
                    unlinkMenuIds.add(node.getId());
                }
                softDeleteIds.add(node.getId());
                affectedRoles.addAll(usage.roles().getOrDefault(node.getId(), List.of()));
                changes.add("删除「" + node.getName() + "」");
                continue;
            }
            if (!Objects.equals(prior.parentKey(), node.parentKey())) {
                changes.add("移动「" + node.getName() + "」");
            } else if (!Objects.equals(prior.getSort(), node.getSort())) {
                changes.add("调整「" + node.getName() + "」的排序");
            }
            if (!Objects.equals(prior.getName(), node.getName())) {
                changes.add("将「" + prior.getName() + "」改名为「" + node.getName() + "」");
            }
            if (!Objects.equals(flag(prior.getEnabled(), true), node.on(node.getEnabled()))) {
                if (node.on(node.getEnabled())) {
                    changes.add("启用「" + node.getName() + "」");
                } else {
                    List<String> affected = usage.packagesUsing().getOrDefault(node.getId(), List.of());
                    if (affected.isEmpty()) {
                        changes.add("停用「" + node.getName() + "」");
                    } else {
                        changes.add("停用「" + node.getName() + "」，影响套餐：" + String.join("、", affected));
                    }
                }
            }
            if (!Objects.equals(blank(prior.getIcon()), blank(node.getIcon()))
                    || !Objects.equals(blank(prior.getPath()), blank(node.getPath()))
                    || !Objects.equals(blank(prior.getPermission()), blank(node.getPermission()))
                    || prior.on(prior.getRequired()) != node.on(node.getRequired())) {
                changes.add("修改「" + node.getName() + "」");
            }
        }
        return new PublishAssessment(changes, softDeleteIds, List.copyOf(unlinkMenuIds), List.copyOf(affectedRoles));
    }

    /**
     * 回滚把历史发布内容放回草稿。当前多出来的菜单标成待删除，发布时再校验引用。
     */
    public static List<CatalogNode> rollbackDraft(List<CatalogNode> currentPublished, List<CatalogNode> release) {
        Map<String, CatalogNode> current = index(currentPublished);
        List<CatalogNode> draft = new ArrayList<>();
        Set<String> releaseIds = new HashSet<>();
        for (CatalogNode node : release) {
            CatalogNode copy = node.copy();
            copy.setPendingDelete(false);
            copy.setPublished(true);
            if (current.containsKey(node.getId())) {
                copy.setSystem(current.get(node.getId()).on(current.get(node.getId()).getSystem()));
            }
            draft.add(copy);
            releaseIds.add(node.getId());
        }
        for (CatalogNode node : currentPublished) {
            if (!releaseIds.contains(node.getId())) {
                CatalogNode copy = node.copy();
                copy.setPendingDelete(true);
                copy.setPublished(true);
                draft.add(copy);
            }
        }
        return draft;
    }

    private static boolean protectedNode(CatalogNode node) {
        return node.on(node.getSystem()) || node.on(node.getRequired());
    }

    private static boolean hasLiveNavChild(CatalogNode node, List<CatalogNode> draft) {
        for (CatalogNode other : draft) {
            if (node.getId().equals(other.parentKey()) && other.navigation() && !other.on(other.getPendingDelete())) {
                return true;
            }
        }
        return false;
    }

    private static void assertParent(CatalogNode node, Map<String, CatalogNode> byId) {
        String parentId = node.parentKey();
        if ("0".equals(parentId)) {
            if (Integer.valueOf(3).equals(node.getType())) {
                throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
            }
            return;
        }
        if (parentId.equals(node.getId())) {
            throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
        }
        CatalogNode parent = byId.get(parentId);
        if (parent == null) {
            throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
        }
        if (isAncestor(node.getId(), parent, byId)) {
            throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
        }
        if (Integer.valueOf(1).equals(parent.getType()) && !Integer.valueOf(3).equals(node.getType())) {
            return;
        }
        if (Integer.valueOf(2).equals(parent.getType()) && Integer.valueOf(3).equals(node.getType())) {
            return;
        }
        throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
    }

    private static boolean isAncestor(String ancestorId, CatalogNode node, Map<String, CatalogNode> byId) {
        String parentId = node.parentKey();
        Set<String> guard = new HashSet<>();
        while (!"0".equals(parentId)) {
            if (!guard.add(parentId)) {
                return false;
            }
            if (ancestorId.equals(parentId)) {
                return true;
            }
            CatalogNode parent = byId.get(parentId);
            if (parent == null) {
                return false;
            }
            parentId = parent.parentKey();
        }
        return false;
    }

    private static int depth(CatalogNode node, Map<String, CatalogNode> byId) {
        int depth = 1;
        String parentId = node.parentKey();
        Set<String> guard = new HashSet<>();
        while (!"0".equals(parentId)) {
            if (!guard.add(parentId)) {
                throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
            }
            CatalogNode parent = byId.get(parentId);
            if (parent == null) {
                throw MenuException.of("MENU_PARENT_INVALID", "无法移动到该位置");
            }
            if (parent.navigation()) {
                depth++;
            }
            parentId = parent.parentKey();
        }
        return depth;
    }

    private static Map<String, CatalogNode> index(List<CatalogNode> nodes) {
        Map<String, CatalogNode> byId = new HashMap<>();
        for (CatalogNode node : nodes) {
            if (node.getId() != null) {
                byId.putIfAbsent(node.getId(), node);
            }
        }
        return byId;
    }

    private static boolean flag(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }
}
