package top.wyhao.web.core.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询参数（请求）
 * <p>统一字段：{@code page} / {@code pageSize}</p>
 */
@Data
@Schema(description = "分页查询条件")
public class PageQuery {

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于 {value}")
    private Integer page = 1;

    /**
     * 每页条数
     */
    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "每页条数不能小于 {value}")
    @Max(value = 1000, message = "每页条数不能超过 {value}")
    private Integer pageSize = 10;

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
}
