
package top.wyhao.admin.system.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.cmn.db.query.Query;
import top.wyhao.cmn.db.query.QueryType;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.List;

/**
 * 角色查询条件
 *

 * @since 2023/2/8 23:04
 */
@Data
@Schema(description = "角色查询条件")
public class RoleQuery extends SortQuery {
    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "测试人员")
    @Query(columns = {"name", "code", "description"}, type = QueryType.LIKE)
    private String description;

    /**
     * 排除的编码列表
     */
    @Schema(description = "排除的编码列表", example = "[super_admin,tenant_admin]")
    @Query(columns = "code", type = QueryType.NOT_IN)
    private List<String> excludeRoleCodes;
}
