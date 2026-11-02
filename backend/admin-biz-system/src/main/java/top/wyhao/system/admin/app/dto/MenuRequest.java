package top.wyhao.system.admin.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import top.wyhao.system.admin.domain.model.MenuType;
import top.wyhao.cmn.core.enums.StatusEnum;

/**
 * 菜单创建或修改请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜单创建或修改请求参数")
public class MenuRequest {



    @NotNull(groups = Update.class)
    private Long id;

    @Schema(description = "类型", example = "2")
    @NotNull(message = "类型无效", groups = Create.class)
    private MenuType type;

    @Schema(description = "图标", example = "user")
    @Length(max = 50, message = "图标长度不能超过 {max} 个字符")
    private String icon;

    @Schema(description = "排序", example = "1")
    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序最小值为 {value}")
    private Integer sort;

    @Schema(description = "权限标识", example = "system:user:list")
    @Length(max = 100, message = "权限标识长度不能超过 {max} 个字符")
    private String permission;

    @Schema(description = "路由地址", example = "/system/user")
    @Length(max = 255, message = "路由地址长度不能超过 {max} 个字符")
    private String path;

    @Schema(description = "组件名称", example = "User")
    @Length(max = 50, message = "组件名称长度不能超过 {max} 个字符")
    private String name;

    @Schema(description = "组件路径", example = "/system/user/index")
    @Length(max = 255, message = "组件路径长度不能超过 {max} 个字符")
    private String component;

    @Schema(description = "重定向地址")
    private String redirect;

    @Schema(description = "是否外链", example = "false")
    private Boolean isExternal;

    @Schema(description = "是否缓存", example = "false")
    private Boolean isCache;

    @Schema(description = "是否隐藏", example = "false")
    private Boolean isHidden;

    @Schema(description = "上级菜单 ID", example = "1000")
    private Long parentId;

    @Schema(description = "状态", example = "1")
    @NotNull(message = "状态无效")
    private StatusEnum status;

    public interface Update extends Default {}
    public interface Create extends Default {}
}
