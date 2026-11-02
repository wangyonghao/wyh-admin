
package top.wyhao.system.admin.app.dto;

import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.wyhao.system.admin.domain.model.MenuType;
import top.wyhao.cmn.core.enums.StatusEnum;

import java.time.LocalDateTime;

/**
 * 菜单响应参数
 */
@Data
@Schema(description = "菜单响应参数")
public class MenuVO {
    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @ExcelProperty(value = "ID", order = 1)
    private Long id;

    /**
     * 创建人
     */
    @JsonIgnore
    private Long createUser;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "超级管理员")
    @ExcelProperty(value = "创建人", order = Integer.MAX_VALUE - 4)
    private String createUserString;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-08-08 08:08:08", type = "string")
    @ExcelProperty(value = "创建时间", order = Integer.MAX_VALUE - 3)
    private LocalDateTime createTime;

    /**
     * 是否禁用修改
     */
    @Schema(description = "是否禁用修改", example = "true")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean disabled;

    /**
     * 名称
     */
    @Schema(description = "名称", example = "用户管理")
    private String name;

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
     * 路由地址
     */
    @Schema(description = "路由地址", example = "/system/user")
    private String path;


    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "/system/user/index")
    private String component;

    /**
     * 重定向地址
     */
    @Schema(description = "重定向地址")
    private String redirect;

    /**
     * 图标
     */
    @Schema(description = "图标", example = "user")
    private String icon;

    /**
     * 是否外链
     */
    @Schema(description = "是否外链", example = "false")
    private Boolean isExternal;

    /**
     * 是否缓存
     */
    @Schema(description = "是否缓存", example = "false")
    private Boolean isCache;

    /**
     * 是否隐藏
     */
    @Schema(description = "是否隐藏", example = "false")
    private Boolean isHidden;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识", example = "system:user:list")
    private String permission;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sort;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private StatusEnum status;
}