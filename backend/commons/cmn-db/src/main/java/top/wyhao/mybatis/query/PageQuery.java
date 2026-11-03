package top.wyhao.mybatis.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * 所有分页查询 DTO 的父类。
 * 业务 DTO 继承它，只需要写自己的查询字段。
 * <p>统一请求字段：{@code page} / {@code pageSize}</p>
 */
@Data
public class PageQuery {

    @Min(value = 1, message = "页码不能小于 1")
    private long page = 1;

    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 500, message = "每页条数不能超过 500")
    private long pageSize = 10;

    /**
     * 排序字段，传实体的【属性名】，支持多字段（逗号分隔，按先后顺序生效）：
     * <pre>
     *   createTime
     *   status,createTime
     *   status asc,createTime desc
     *   status:asc,createTime:desc
     *   +status,-createTime
     * </pre>
     * 未单独指定方向的项，方向取 {@link #orderDir}。
     */
    private String orderBy;

    /** 默认排序方向：asc / desc，仅对未单独指定方向的排序项生效 */
    private String orderDir = "desc";

    /** 是否需要查询总数，导出等场景可置 false 省掉一次 count */
    private boolean searchCount = true;

    /**
     * 是否在排序末尾自动追加主键，保证同值行的翻页结果稳定。
     * 默认开启，除非确认排序字段本身唯一。
     */
    private boolean stableSort = true;

    public boolean isAsc() {
        return "asc".equalsIgnoreCase(orderDir);
    }

    /** 解析后的排序项，便于 Service 层做额外判断 */
    public List<Sort> sorts() {
        return SortParser.parse(orderBy, isAsc());
    }
}
