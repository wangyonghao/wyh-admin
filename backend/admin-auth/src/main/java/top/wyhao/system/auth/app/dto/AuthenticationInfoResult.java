package top.wyhao.system.auth.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.identity.client.UserProfile;

import java.util.List;

/**
 * 用户认证信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户认证信息")
public class AuthenticationInfoResult {



    /**
     * 用户信息
     */
    @Schema(description = "用户信息", example = "用户信息")
    private UserProfile user;

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

    /**
     * 用户菜单
     */
    @Schema(description = "用户菜单", example = "")
    private List<?> menus;
}
