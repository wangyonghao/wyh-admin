
package top.wyhao.system.open.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.mybatis.query.Query;
import top.wyhao.mybatis.query.SortableQuery;

/**
 * 应用查询条件


 * @since 2024/10/17 16:03
 */
@Data
@Schema(description = "应用查询条件")
public class AppQuery extends SortableQuery {

    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "应用1")
    @Query(field = "name,description", type = Query.Type.LIKE)
    private String description;

    /**
     * 排序条件
     */
    @Schema(description = "排序条件", example = "createTime,desc")
    private String[] sort;
}