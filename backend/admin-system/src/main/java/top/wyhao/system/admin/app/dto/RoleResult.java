package top.wyhao.system.admin.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.cmn.core.enums.DataScopeEnum;

import java.time.LocalDateTime;

/**
 * 角色响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色响应参数")
public class RoleResult {

    @Schema(description = "ID", example = "1")
    private Long id;

    @JsonIgnore
    private Long createUser;

    @Schema(description = "创建人", example = "超级管理员")
    private String createUserString;

    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime createTime;

    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    @JsonIgnore
    private Long updateUser;

    @Schema(description = "修改人", example = "李四")
    private String updateUserString;

    @Schema(description = "修改时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime updateTime;

    @Schema(description = "名称", example = "测试人员")
    private String name;

    @Schema(description = "编码", example = "test")
    private String code;

    @Schema(description = "数据权限", example = "5")
    private DataScopeEnum dataScope;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "是否为系统内置数据", example = "false")
    private Boolean isBuiltin;

    @Schema(description = "描述", example = "测试人员描述信息")
    private String description;
}
