package top.wyhao.mybatis.query;

import lombok.Data;

import java.util.List;

/**
 * 所有【查询条件 DTO】的父类：只包含排序，不包含分页参数。
 *
 * <p>之所以要把排序从 {@link PageQuery} 里独立出来，是因为条件+排序是可以脱离分页复用的：
 * 分页查询、导出、下拉选项加载、批量处理都需要同一套「筛选 + 排序」逻辑，
 * 但只有分页查询需要 page/pageSize。如果条件 DTO 直接继承 PageQuery，
 * 导出方法的入参就会被迫背上两个业务上无意义的分页字段。</p>
 *
 * <p>用法：业务查询 DTO（如 UserQuery）继承这个类并加 {@code @Query} 注解字段；
 * 需要分页的接口，Controller 方法再单独接收一个 {@link PageParam} 参数，
 * Spring MVC 会从同一份请求参数里分别绑定两个对象。</p>
 */
@Data
public abstract class SortableQuery {

    /**
     * 排序字段，传实体的【属性名】，支持多字段（逗号分隔，按先后顺序生效），
     * 语法见 {@link SortParser}。
     */
    private String orderBy;

    /** 默认排序方向：asc / desc，仅对未单独指定方向的排序项生效 */
    private String orderDir = "desc";

    public boolean isAsc() {
        return "asc".equalsIgnoreCase(orderDir);
    }

    /** 解析后的排序项，Service 层需要时可以直接拿 */
    public List<Sort> sorts() {
        return SortParser.parse(orderBy, isAsc());
    }
}