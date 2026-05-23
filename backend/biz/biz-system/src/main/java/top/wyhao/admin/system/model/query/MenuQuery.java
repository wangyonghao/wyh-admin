
package top.wyhao.admin.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.cmn.db.query.Query;
import top.wyhao.cmn.db.query.QueryType;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.List;

/**
 * 菜单查询条件
 *

 * @since 2023/2/15 20:21
 */
@Data
@NoArgsConstructor
@Schema(description = "菜单查询条件")
public class MenuQuery extends SortQuery {
    /**
     * 标题
     */
    @Schema(description = "标题", example = "用户管理")
    @Query(type = QueryType.LIKE)
    private String title;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @Query(type = QueryType.EQ)
    private String status;

    /**
     * 排除的菜单 ID 列表
     */
    @Schema(hidden = true, description = "排除的菜单 ID 列表", example = "[9000]")
    @Query(columns = "id", type = QueryType.NOT_IN)
    private List<Long> excludeMenuIdList;
}
