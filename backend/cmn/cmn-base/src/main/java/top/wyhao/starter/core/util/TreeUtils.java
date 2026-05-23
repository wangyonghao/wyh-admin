
package top.wyhao.starter.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 通用树形结构工具类
 *
 * <p>
 * 扩展 TreeUtil 封装树构建
 * </p>
 *

 */
public class TreeUtils {

    private TreeUtils() {
    }

    /**
     * 扁平列表 转 树形结构（支持自定义字段）
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * List<Menu> flatList = List.of(
     *     new Menu(1L, 0L, "根菜单"),
     *     new Menu(2L, 1L, "子菜单1"),
     *     new Menu(3L, 1L, "子菜单2"),
     *     new Menu(4L, 2L, "孙菜单")
     * );
     * // 调用 buildTree 构建树
     * List<Menu> tree = TreeUtils.buildTree(
     *         flatList,
     *         Menu::getMenuId,      // 主键字段
     *         Menu::getPId,         // 父级ID字段
     *         Menu::getChildList,   // 获取子节点列表
     *         Menu::setChildList,   // 设置子节点列表
     * );
     * }</pre>
     *
     * @param list           原始扁平数据
     * @param idGetter       获取id的方法
     * @param parentIdGetter 获取parentId的方法
     * @param childrenSetter 设置children的方法
     * @return 树形结构
     */
    public static <T, ID> List<T> flatToTree(
            List<T> list,
            Function<T, ID> idGetter,
            Function<T, ID> parentIdGetter,
            Function<T, List<T>> childrenGetter,
            BiConsumer<T, List<T>> childrenSetter) {
        // 1. ID -> 节点映射
        Map<ID, T> nodeMap = new HashMap<>();
        for (T item : list) {
            nodeMap.put(idGetter.apply(item), item);
        }

        List<T> tree = new ArrayList<>();

        // 2. 构建父子关系
        for (T node : list) {
            ID parentId = parentIdGetter.apply(node);
            // 根节点
            if (parentId == null || !nodeMap.containsKey(parentId)) {
                tree.add(node);
                continue;
            }
            // 找到父节点
            T parent = nodeMap.get(parentId);
            if (parent == null) {
                continue;
            }
            List<T> children = childrenGetter.apply(parent);
            if (children == null) {
                children = new ArrayList<>();
                childrenSetter.accept(parent, children);
            }
            children.add(node);
        }
        return tree;
    }
}
