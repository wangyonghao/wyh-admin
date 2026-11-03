package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.wyhao.mybatis.query.Query;
import top.wyhao.mybatis.query.SortableQuery;
import top.wyhao.cmn.core.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户查询条件
 *
 * @since 2023/2/20 21:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询条件")
public class UserQuery extends SortableQuery {

    @Schema(description = "关键词", example = "zhangsan")
    /** 关键字：一个字段同时 LIKE 三列，自动组装成 (username like ? or displayName like ? or phone like ?) */
    @Query(type = Query.Type.LIKE, field = "username,displayName,phone")
    private String keyword;

    @Schema(description = "状态", example = "1")
    private StatusEnum status;

    @Schema(description = "创建时间", example = "2023-08-08 00:00:00,2023-08-08 23:59:59")
    @Size(max = 2, message = "创建时间必须是一个范围")
    private List<LocalDateTime> createTime;

    @Schema(description = "部门 ID", example = "1")
    private Long departmentId;

    @Schema(description = "用户 ID 列表", example = "[1,2,3]")
    private List<Long> userIds;

    @Schema(description = "角色 ID", example = "1")
    private Long roleId;

    @Schema(description = "排序条件", example = "createTime,desc")
    private String[] sort;
}
