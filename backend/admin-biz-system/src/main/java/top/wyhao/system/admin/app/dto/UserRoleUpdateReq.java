
package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 用户角色修改请求参数
 *

 * @since 2023/2/24 23:05
 */
@Data
@Schema(description = "用户角色修改请求参数")
public class UserRoleUpdateReq {
    /**
     * 角色 ID 列表
     */
    @Schema(description = "所属角色", example = "1,2")
    @NotEmpty(message = "所属角色不能为空")
    private List<Long> roleIds;
}
