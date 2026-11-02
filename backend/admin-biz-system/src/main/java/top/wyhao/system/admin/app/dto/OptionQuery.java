package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.wyhao.mybatis.query.Query;
import top.wyhao.mybatis.query.SortableQuery;

/**
 * 选项查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "选项查询条件")
public class OptionQuery extends SortableQuery {

    @Schema(description = "关键词（类型、标签、值、描述）")
    @Query(field = "optionType,label,value,description", type = Query.Type.LIKE)
    private String keyword;

    @Schema(description = "选项类型", example = "notice_type")
    @Query
    private String optionType;

    @Schema(description = "是否启用", example = "true")
    @Query
    private Boolean enabled;

    /**
     * 列表页状态筛选，对应 enabled
     */
    @Schema(description = "状态", example = "true")
    @Query(field = "enabled")
    private Boolean status;
}
