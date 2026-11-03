package top.wyhao.mybatis.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 纯分页参数。
 * <p>统一请求字段：{@code page} / {@code pageSize}</p>
 */
@Data
public class PageParam {

    @Min(value = 1, message = "页码不能小于 1")
    private long page = 1;

    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 1000, message = "每页条数不能超过 1000")
    private long pageSize = 10;

    /** 是否需要查询总数，导出等只需要列表的场景可置 false 省掉一次 count */
    private boolean searchCount = true;

    /**
     * 是否在排序末尾自动追加主键，保证同值行的翻页结果稳定。
     * 默认开启，除非确认排序字段本身唯一，或者本来就不需要翻页（如导出）。
     */
    private boolean stableSort = true;
}
