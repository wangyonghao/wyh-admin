package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 角色成员删除请求参数
 */
@Data
@Schema(description = "角色成员删除请求参数")
public class RoleMemberRemoveRequest {

    @Schema(description = "ID", example = "[1,2]")
    @NotEmpty(message = "ID 不能为空")
    private List<Long> userIds;
}
