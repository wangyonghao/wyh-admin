package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.mybatis.query.Query;

import java.util.List;

/**
 * 角色查询条件
 */
@Data
@Schema(description = "角色查询条件")
public class RoleQuery {

    @Schema(description = "关键词", example = "测试人员")
    @Query(field = "name,code,description", type = Query.Type.LIKE)
    private String description;

    @Schema(description = "排除的编码列表", example = "[super_admin,tenant_admin]")
    @Query(field = "code", type = Query.Type.NOT_IN)
    private List<String> excludeRoleCodes;

    @Schema(description = "排序条件", example = "createTime,desc")
    private String[] sort;
}
