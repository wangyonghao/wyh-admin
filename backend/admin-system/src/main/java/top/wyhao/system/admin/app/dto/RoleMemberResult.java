package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.cmn.core.enums.GenderEnum;
import top.wyhao.cmn.core.enums.StatusEnum;

import java.util.List;

/**
 * 角色关联用户响应参数
 */
@Data
@Schema(description = "角色关联用户响应参数")
public class RoleMemberResult {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "角色 ID", example = "1")
    private Long roleId;

    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "显示名称", example = "张三")
    private String displayName;

    @Schema(description = "性别", example = "1")
    private GenderEnum gender;

    @Schema(description = "用户头像文件 ID", example = "1001")
    private Long avatar;

    @Schema(description = "状态", example = "1")
    private StatusEnum status;

    @Schema(description = "是否为系统内置数据", example = "false")
    private Boolean isBuiltin;

    @Schema(description = "描述", example = "测试人员描述信息")
    private String description;

    @Schema(description = "部门 ID", example = "5")
    private Long departmentId;

    @Schema(description = "所属部门", example = "测试部")
    private String departmentName;

    @Schema(description = "角色 ID 列表", example = "2")
    private List<Long> roleIds;

    @Schema(description = "角色名称列表", example = "测试人员")
    private List<String> roleNames;
}
