
package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.system.admin.domain.model.MenuType;

import java.util.List;

/**
 * 角色权限树响应参数
 */
@Data
@Schema(description = "菜单树")
public class MenuTreeVO {

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "用户管理")
    private String name;

    /**
     * 图标
     */
    private String icon;
    /**
     * 路径
     */
    private String path;
    /**
     * 组件位置
     */
    private String component;

    /**
     * 上级菜单 ID
     */
    @Schema(description = "上级菜单 ID", example = "1000")
    private Long parentId;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "2")
    private MenuType type;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识", example = "system:user:list")
    private String permission;

    /**
     * 子菜单列表
     */
    @Schema(description = "子菜单")
    private List<MenuTreeVO> children;
}
