package top.wyhao.mybatis.query;

/**
 * 单个排序项。
 *
 * @param property 实体属性名（不是列名），如 createTime
 * @param asc      true 升序，false 降序
 */
public record Sort(String property, boolean asc) {

    public static Sort asc(String property) {
        return new Sort(property, true);
    }

    public static Sort desc(String property) {
        return new Sort(property, false);
    }
}
