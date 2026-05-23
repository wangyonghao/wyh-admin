
package top.wyhao.admin.open.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.cmn.db.query.Query;
import top.wyhao.cmn.db.query.QueryType;
import top.wyhao.starter.web.core.model.SortQuery;

/**
 * 应用查询条件


 * @since 2024/10/17 16:03
 */
@Data
@Schema(description = "应用查询条件")
public class AppQuery extends SortQuery {

    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "应用1")
    @Query(columns = {"name", "description"}, type = QueryType.LIKE)
    private String description;
}