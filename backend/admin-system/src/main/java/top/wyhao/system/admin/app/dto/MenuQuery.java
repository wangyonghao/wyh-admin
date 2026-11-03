package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.mybatis.query.Query;

import java.util.List;

/**
 * 菜单查询条件
 *
 * @since 2023/2/15 20:21
 */
@Data
@Schema(description = "菜单查询条件")
public class MenuQuery {

    @Schema(description = "标题", example = "用户管理")
    @Query(type = Query.Type.LIKE)
    private String title;

    @Schema(description = "状态", example = "1")
    @Query(type = Query.Type.EQ)
    private String status;

    @Schema(hidden = true, description = "排除的菜单 ID 列表", example = "[9000]")
    @Query(field = "id", type = Query.Type.NOT_IN)
    private List<Long> excludeMenuIdList;

    @Schema(description = "排序条件", example = "createTime,desc")
    private String sort;
}
