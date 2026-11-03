package top.wyhao.mybatis.query;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * 类型安全地声明【默认排序】，支持多字段链式书写：
 *
 * <pre>
 * PageFactory.build(query, User.class,
 *         Sorts.asc(User::getStatus).thenDesc(User::getCreateTime));
 * </pre>
 */
public final class Sorts {

    private final List<Sort> items = new ArrayList<>();

    private Sorts() {
    }

    public static <T> Sorts asc(SFunction<T, ?> fn) {
        return new Sorts().thenAsc(fn);
    }

    public static <T> Sorts desc(SFunction<T, ?> fn) {
        return new Sorts().thenDesc(fn);
    }

    /** 按属性名声明，适用于关联查询中主表字段不方便用方法引用的场景 */
    public static Sorts by(Sort... sorts) {
        Sorts s = new Sorts();
        s.items.addAll(List.of(sorts));
        return s;
    }

    public <T> Sorts thenAsc(SFunction<T, ?> fn) {
        items.add(Sort.asc(ColumnResolver.propertyOf(fn)));
        return this;
    }

    public <T> Sorts thenDesc(SFunction<T, ?> fn) {
        items.add(Sort.desc(ColumnResolver.propertyOf(fn)));
        return this;
    }

    public List<Sort> items() {
        return List.copyOf(items);
    }
}
