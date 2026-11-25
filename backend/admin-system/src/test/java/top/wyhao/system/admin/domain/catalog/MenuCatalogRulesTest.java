package top.wyhao.system.admin.domain.catalog;

import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.domain.exception.MenuException;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuCatalogRulesTest {

    @Test
    void shouldReplaceTemporaryIdsAndKeepParent() {
        CatalogNode parent = dir("tmp-a", "0", "组织");
        CatalogNode page = page("tmp-b", "tmp-a", "成员");
        AtomicLong ids = new AtomicLong(10);

        List<CatalogNode> assigned = MenuCatalogRules.assignIds(List.of(parent, page), ids::getAndIncrement);

        assertThat(assigned).extracting(CatalogNode::getId).containsExactly("10", "11");
        assertThat(assigned.get(1).getParentId()).isEqualTo("10");
        assertThat(assigned.get(0).getCode()).isEqualTo("m10");
    }

    @Test
    void shouldKeepPublishedCodeAndRequiredFlag() {
        CatalogNode published = page("1", "0", "首页");
        published.setCode("home");
        published.setRequired(true);
        published.setSystem(true);
        published.setPublished(true);
        CatalogNode draft = page("1", "0", "首页");
        draft.setCode("changed");
        draft.setRequired(false);
        draft.setSystem(false);

        CatalogNode sealed = MenuCatalogRules.seal(List.of(draft), List.of(published)).get(0);

        assertThat(sealed.getCode()).isEqualTo("home");
        assertThat(sealed.on(sealed.getRequired())).isTrue();
        assertThat(sealed.on(sealed.getSystem())).isTrue();
    }

    @Test
    void shouldKeepPublishedTypeAndDropDirectoryPermission() {
        CatalogNode published = page("1", "0", "订单");
        published.setType(2);
        CatalogNode draft = page("1", "0", "订单");
        draft.setType(1);
        draft.setPermission("system:order:list");

        CatalogNode sealed = MenuCatalogRules.seal(List.of(draft), List.of(published)).get(0);

        assertThat(sealed.getType()).isEqualTo(2);
        assertThat(sealed.getPermission()).isEqualTo("system:order:list");

        CatalogNode directory = dir("tmp-a", "0", "新目录");
        directory.setPermission("system:dir");
        CatalogNode created = MenuCatalogRules.seal(List.of(directory), List.of()).get(0);

        assertThat(created.getType()).isEqualTo(1);
        assertThat(created.getPermission()).isNull();
    }

    @Test
    void shouldDropUnpublishedDeleteAndMarkButtons() {
        CatalogNode page = page("1", "0", "订单");
        page.setPublished(true);
        page.setPendingDelete(true);
        CatalogNode button = button("2", "1", "新增");
        button.setPublished(true);
        CatalogNode fresh = page("tmp", "0", "草稿");
        fresh.setPendingDelete(true);

        List<CatalogNode> prepared = MenuCatalogRules.prepare(List.of(page, button, fresh));

        assertThat(prepared).extracting(CatalogNode::getId).containsExactly("1", "2");
        assertThat(prepared.get(1).on(prepared.get(1).getPendingDelete())).isTrue();
    }

    @Test
    void shouldRejectPageNestedInPage() {
        CatalogNode parent = page("1", "0", "订单");
        CatalogNode child = page("2", "1", "详情");

        assertThatThrownBy(() -> MenuCatalogRules.assertStructure(List.of(parent, child)))
                .isInstanceOf(MenuException.class)
                .extracting(ex -> ((MenuException) ex).getCode())
                .isEqualTo("MENU_PARENT_INVALID");
    }

    @Test
    void shouldRejectFourthLevel() {
        CatalogNode a = dir("1", "0", "一");
        CatalogNode b = dir("2", "1", "二");
        CatalogNode c = dir("3", "2", "三");
        CatalogNode d = page("4", "3", "四");

        assertThatThrownBy(() -> MenuCatalogRules.assertStructure(List.of(a, b, c, d)))
                .isInstanceOf(MenuException.class)
                .extracting(ex -> ((MenuException) ex).getCode())
                .isEqualTo("MENU_DEPTH_INVALID");
    }

    @Test
    void shouldAllowPageAtRootAndRejectProtectedDelete() {
        CatalogNode home = page("1", "0", "首页");
        home.setRequired(true);
        home.setPublished(true);
        home.setPendingDelete(true);
        MenuCatalogRules.assertStructure(List.of(home));

        assertThatThrownBy(() -> MenuCatalogRules.assess(List.of(home), List.of(home), List.of(), MenuUsage.empty()))
                .isInstanceOf(MenuException.class)
                .extracting(ex -> ((MenuException) ex).getCode())
                .isEqualTo("MENU_PROTECTED");
    }

    @Test
    void shouldRequireRouteOnlyForPages() {
        CatalogNode directory = dir("1", "0", "租户域");
        directory.setPath(null);
        MenuCatalogRules.assertStructure(List.of(directory));

        CatalogNode page = page("2", "0", "首页");
        page.setPath(" ");
        assertThatThrownBy(() -> MenuCatalogRules.assertStructure(List.of(page)))
                .isInstanceOf(MenuException.class)
                .extracting(ex -> ((MenuException) ex).getCode())
                .isEqualTo("MENU_PATH_REQUIRED");
    }

    @Test
    void shouldRejectDeleteWhenDirectoryStillHasPages() {
        CatalogNode dir = dir("1", "0", "业务");
        dir.setPublished(true);
        dir.setPendingDelete(true);
        CatalogNode page = page("2", "1", "订单");
        page.setPublished(true);

        assertThatThrownBy(() -> MenuCatalogRules.assess(List.of(dir, page), List.of(dir, page), List.of(), MenuUsage.empty()))
                .isInstanceOf(MenuException.class)
                .extracting(ex -> ((MenuException) ex).getCode())
                .isEqualTo("MENU_HAS_CHILDREN");
    }

    @Test
    void shouldBlockDeleteReferencedByPublishedPackageAndAskForRoles() {
        CatalogNode page = page("8", "0", "订单");
        page.setPublished(true);
        page.setPendingDelete(true);
        MenuUsage blocked = new MenuUsage(java.util.Map.of("8", List.of("专业版")), java.util.Map.of(), java.util.Map.of(), java.util.Map.of());

        assertThatThrownBy(() -> MenuCatalogRules.assess(List.of(page), List.of(page), List.of(), blocked))
                .isInstanceOf(MenuException.class)
                .extracting(ex -> ((MenuException) ex).getCode())
                .isEqualTo("MENU_PACKAGE_BLOCKED");

        MenuUsage loose = new MenuUsage(
                java.util.Map.of(),
                java.util.Map.of("8", List.of("行业版")),
                java.util.Map.of("8", List.of("运营")),
                java.util.Map.of());
        PublishAssessment assessment = MenuCatalogRules.assess(List.of(page), List.of(copyLive(page)), List.of(), loose);

        assertThat(assessment.confirmRequired()).isTrue();
        assertThat(assessment.affectedRoleNames()).containsExactly("运营");
        assertThat(assessment.unlinkMenuIds()).containsExactly("8");
        assertThat(assessment.changes()).contains("删除「订单」");
    }

    @Test
    void shouldDeletePlatformMenuEvenWhenPackagesStillLinkIt() {
        CatalogNode directory = dir("1", "0", "平台域");
        directory.setPublished(true);
        directory.setPendingDelete(true);
        MenuUsage linked = new MenuUsage(
                java.util.Map.of("1", List.of("基础套餐", "演示套餐-W3")),
                java.util.Map.of(),
                java.util.Map.of("1", List.of("超级管理员")),
                java.util.Map.of("1", List.of("基础套餐")));

        PublishAssessment assessment = MenuCatalogRules.assess(
                List.of(directory), List.of(directory), List.of(), linked.withoutPackages());

        assertThat(assessment.changes()).containsExactly("删除「平台域」");
        assertThat(assessment.affectedRoleNames()).containsExactly("超级管理员");
        assertThat(assessment.unlinkMenuIds()).isEmpty();
    }

    @Test
    void shouldDescribeDisableImpact() {
        CatalogNode before = page("3", "0", "大屏");
        before.setPublished(true);
        before.setEnabled(true);
        CatalogNode after = copyLive(before);
        after.setEnabled(false);
        MenuUsage usage = new MenuUsage(java.util.Map.of(), java.util.Map.of(), java.util.Map.of(), java.util.Map.of("3", List.of("大屏增购包")));

        PublishAssessment assessment = MenuCatalogRules.assess(List.of(after), List.of(before), List.of(), usage);

        assertThat(assessment.changes()).containsExactly("停用「大屏」，影响套餐：大屏增购包");
    }

    @Test
    void shouldRejectReusedCode() {
        CatalogNode retired = page("1", "0", "旧页面");
        retired.setCode("m1");
        CatalogNode created = page("9", "0", "新页面");
        created.setCode("m1");

        assertThatThrownBy(() -> MenuCatalogRules.assess(List.of(created), List.of(), List.of(retired), MenuUsage.empty()))
                .isInstanceOf(MenuException.class)
                .extracting(ex -> ((MenuException) ex).getCode())
                .isEqualTo("MENU_CODE_REUSED");
    }

    @Test
    void shouldAllowRenameOfRetiredMenu() {
        CatalogNode retired = page("911466798988136471", "0", "租户菜单");
        retired.setCode("m911466798988136471");
        retired.setPublished(true);
        CatalogNode renamed = copyLive(retired);
        renamed.setName("企业菜单");

        PublishAssessment assessment = MenuCatalogRules.assess(
                List.of(renamed), List.of(), List.of(retired), MenuUsage.empty());

        assertThat(assessment.changes()).contains("将「租户菜单」改名为「企业菜单」");
    }

    @Test
    void shouldMarkMenusAddedAfterReleaseAsPendingDelete() {
        CatalogNode kept = page("1", "0", "首页");
        kept.setPublished(true);
        CatalogNode extra = page("2", "0", "实验");
        extra.setPublished(true);

        List<CatalogNode> draft = MenuCatalogRules.rollbackDraft(List.of(kept, extra), List.of(kept));

        assertThat(draft).hasSize(2);
        CatalogNode pending = draft.stream().filter(node -> "2".equals(node.getId())).findFirst().orElseThrow();
        assertThat(pending.on(pending.getPendingDelete())).isTrue();
    }

    private static CatalogNode dir(String id, String parentId, String name) {
        CatalogNode node = node(id, parentId, name, 1);
        node.setPath("/" + id);
        return node;
    }

    private static CatalogNode page(String id, String parentId, String name) {
        CatalogNode node = node(id, parentId, name, 2);
        node.setPath("/" + id);
        return node;
    }

    private static CatalogNode button(String id, String parentId, String name) {
        return node(id, parentId, name, 3);
    }

    private static CatalogNode node(String id, String parentId, String name, int type) {
        CatalogNode node = new CatalogNode();
        node.setId(id);
        node.setCode("m" + id);
        node.setParentId(parentId);
        node.setName(name);
        node.setType(type);
        node.setSort(1);
        node.setEnabled(true);
        node.setPublished(false);
        node.setPendingDelete(false);
        return node;
    }

    private static CatalogNode copyLive(CatalogNode node) {
        CatalogNode copy = node.copy();
        copy.setPendingDelete(false);
        return copy;
    }
}
