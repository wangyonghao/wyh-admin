
package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.mybatis.query.Query;
import top.wyhao.cmn.core.enums.StatusEnum;

/**
 * 套餐查询条件
 *
 * @since 2024/11/26 11:25
 */
@Data
@Schema(description = "套餐查询条件")
public class PackageQuery {
    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "初级套餐")
    @Query(field = "name,description", type = Query.Type.LIKE)
    private String description;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "初级套餐")
    @Query(type = Query.Type.LIKE)
    private String name;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @Query(type = Query.Type.EQ)
    private StatusEnum status;

    /**
     * 排序条件
     */
    @Schema(description = "排序条件", example = "createTime,desc")
    private String[] sort;
}