package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色成员查询条件
 */
@Data
@Schema(description = "角色成员查询条件")
public class RoleMemberQuery {

    @Schema(description = "角色 ID", example = "1")
    private Long roleId;

    @Schema(description = "关键词", example = "zhangsan")
    private String keyword;
}
