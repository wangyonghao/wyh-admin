
package top.wyhao.admin.system.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.cmn.db.query.Query;
import top.wyhao.cmn.db.query.QueryType;

/**
 * 字典查询条件
 *

 * @since 2026/5/13
 */
@Data
@Schema(description = "字典查询条件")
public class DictQuery{
    /**
     * 关键词
     */
    @Schema(description = "关键词")
    @Query(columns = {"dict_type", "label", "value", "description"}, type = QueryType.LIKE)
    private String description;

    /**
     * 字典类型
     */
    @Schema(description = "字典类型", example = "notice_type")
    @Query(type = QueryType.EQ)
    private String dictType;

    @Schema(description = "是否启用")
    @Query(type = QueryType.EQ)
    private Boolean enabled;
}