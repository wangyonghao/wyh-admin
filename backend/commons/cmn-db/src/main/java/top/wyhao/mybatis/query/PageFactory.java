package top.wyhao.mybatis.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * (PageParam, SortableQuery) -> MyBatis-Plus Page 的转换工厂。
 *
 * <p>排序规则：</p>
 * <ol>
 *   <li>前端传了 orderBy，用前端的（支持多字段，见 {@link SortParser}）；</li>
 *   <li>否则用代码里声明的默认排序；</li>
 *   <li>若排序字段里不含主键且 {@code pageParam.isStableSort()}，自动追加主键兜底，保证翻页结果稳定。</li>
 * </ol>
 *
 * <p>所有排序字段都经过 {@link ColumnResolver} 校验，非实体字段直接抛异常。</p>
 *
 * <p>提供两组重载：条件与分页分离的 {@code (PageParam, SortableQuery)} 是推荐用法
 * （条件 DTO 可以脱离分页复用于导出等场景）；接收单个 {@link PageQuery} 的重载
 * 是给不需要复用、图省事的简单查询用的便捷写法。</p>
 */
public final class PageFactory {

    private PageFactory() {
    }

    // ------------------------------------------------------------------
    // 推荐写法：分页参数与查询条件分离
    // ------------------------------------------------------------------

    /** 单表分页，无默认排序 */
    public static <T> Page<T> build(PageParam pageParam, SortableQuery query, Class<T> entityClass) {
        return create(pageParam, query, entityClass, null, List.of());
    }

    /** 单表分页 + 单字段默认排序（倒序） */
    public static <T> Page<T> build(PageParam pageParam, SortableQuery query,
                                    Class<T> entityClass, SFunction<T, ?> defaultOrder) {
        return create(pageParam, query, entityClass, null, Sorts.desc(defaultOrder).items());
    }

    /**
     * 单表分页 + 多字段默认排序：
     * <pre>Sorts.asc(User::getStatus).thenDesc(User::getCreateTime)</pre>
     */
    public static <T> Page<T> build(PageParam pageParam, SortableQuery query,
                                    Class<T> entityClass, Sorts defaultOrders) {
        return create(pageParam, query, entityClass, null, defaultOrders.items());
    }

    /** 关联查询分页：排序字段仍按主表实体校验，并自动加表别名 */
    public static <R> Page<R> buildFor(PageParam pageParam, SortableQuery query,
                                       Class<?> metaEntity, String alias, Sorts defaultOrders) {
        return create(pageParam, query, metaEntity, alias, defaultOrders == null ? List.of() : defaultOrders.items());
    }

    // ------------------------------------------------------------------

    private static <R> Page<R> create(PageParam pageParam, SortableQuery query,
                                      Class<?> metaEntity, String alias, List<Sort> defaultOrders) {
        Page<R> page = new Page<>(pageParam.getPage(), pageParam.getPageSize(), pageParam.isSearchCount());

        List<Sort> sorts = query.sorts();
        if (sorts.isEmpty()) {
            sorts = defaultOrders;
        }

        List<Sort> finalSorts = new ArrayList<>(sorts);
        appendTieBreaker(pageParam, metaEntity, finalSorts);

        for (Sort sort : finalSorts) {
            String column = ColumnResolver.resolve(metaEntity, sort.property(), alias);
            page.addOrder(sort.asc() ? OrderItem.asc(column) : OrderItem.desc(column));
        }
        return page;
    }

    /**
     * 按非唯一列排序时，数据库对同值行的返回顺序不确定，会出现「翻页时同一条记录重复出现」。
     * 追加主键兜底可以消除这个问题。
     */
    private static void appendTieBreaker(PageParam pageParam, Class<?> metaEntity, List<Sort> sorts) {
        if (!pageParam.isStableSort() || sorts.isEmpty()) {
            return;
        }
        String keyProperty = ColumnResolver.keyProperty(metaEntity);
        if (keyProperty == null) {
            return;
        }
        boolean present = sorts.stream().anyMatch(s -> keyProperty.equals(s.property()));
        if (!present) {
            sorts.add(Sort.desc(keyProperty));
        }
    }
}

