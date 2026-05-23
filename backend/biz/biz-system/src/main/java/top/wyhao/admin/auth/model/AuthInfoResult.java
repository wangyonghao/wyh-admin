
package top.wyhao.admin.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.admin.system.model.vo.MenuTreeVO;
import top.wyhao.admin.system.model.vo.user.UserDetailResult;

import java.util.List;

/**
 * 用户认证信息

 */
@Data
@Schema(description = "用户认证信息")
public class AuthInfoResult {
    /**
     * 用户信息
     */
    @Schema(description = "用户信息", example = "用户信息")
    private UserDetailResult user;
    /**
     * 角色编码集合
     */
    @Schema(description = "角色编码集合", example = "[\"test\"]")
    private List<String> roles;
    /**
     * 权限码集合
     */
    @Schema(description = "权限码集合", example = "[\"system:user:list\",\"system:user:create\"]")
    private List<String> permissions;

    @Schema(description = "用户菜单", example = "")
    private List<MenuTreeVO> menus;
}
