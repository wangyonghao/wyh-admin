package top.wyhao.cmn.core.util;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TreeUtilsTest {

    @Test
    public void testFlatToTree_normal() {
        // 1. 构造扁平数据
        Menu m1 = new Menu();
        m1.setMenuId(1L);
        m1.setPId(0L);
        m1.setName("根菜单");

        Menu m2 = new Menu();
        m2.setMenuId(2L);
        m2.setPId(1L);
        m2.setName("子菜单1");

        Menu m3 = new Menu();
        m3.setMenuId(3L);
        m3.setPId(1L);
        m3.setName("子菜单2");

        Menu m4 = new Menu();
        m4.setMenuId(4L);
        m4.setPId(2L);
        m4.setName("孙菜单");

        List<Menu> flatList = List.of(m1, m2, m3, m4);

        // 2. 构建树（自定义字段）
        List<Menu> tree = TreeUtils.flatToTree(
                flatList,
                Menu::getMenuId,
                Menu::getPId,
                Menu::getChildList,
                Menu::setChildList
        );

        // 3. 断言
        assertEquals(1, tree.size());
        Menu root = tree.get(0);
        assertEquals(1L, root.getMenuId());
        assertEquals(0L, root.getPId());

        List<Menu> children = root.getChildList();
        assertEquals(2, children.size());

        Menu m2Found = children.stream().filter(m -> m.getMenuId() == 2).findFirst().orElse(null);
        assertNotNull(m2Found);
        assertEquals(1, m2Found.getChildList().size());
        assertEquals(4L, m2Found.getChildList().get(0).getMenuId());
    }

    @Test
    public void testFlatToTree_emptyList() {
        List<Menu> tree = TreeUtils.flatToTree(
                List.of(),
                Menu::getMenuId,
                Menu::getPId,
                Menu::getChildList,
                Menu::setChildList
        );
        assertTrue(tree.isEmpty());
    }

    @Test
    public void testFlatToTree_onlyRoot() {
        Menu m1 = new Menu();
        m1.setMenuId(1L);
        m1.setPId(0L);

        List<Menu> tree = TreeUtils.flatToTree(
                List.of(m1),
                Menu::getMenuId,
                Menu::getPId,
                Menu::getChildList,
                Menu::setChildList
        );

        assertEquals(1, tree.size());
        assertNull(tree.get(0).getChildList()); // 没子节点保持null
    }

    @Data
    static class Menu {
        private Long menuId;    // 自定义 id
        private Long pId;       // 自定义 parentId
        private String name;
        private List<Menu> childList; // 自定义 children
    }
}

