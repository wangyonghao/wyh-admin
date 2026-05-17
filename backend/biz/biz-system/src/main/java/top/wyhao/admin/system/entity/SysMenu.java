
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.admin.system.model.enums.MenuType;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.cmn.db.model.BaseDO;

/**
 * 菜单实体
 *
 * @author Charles7c
 * @since 2023/2/15 20:14
 */
@Data
@TableName("sys_menu")
public class SysMenu extends BaseDO {
    /**
     * 上级菜单 ID
     */
    private Long parentId;

    /**
     * 类型
     */
    private MenuType type;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 重定向地址
     */
    private String redirect;

    /**
     * 图标
     */
    private String icon;

    /**
     * 是否外链
     */
    private Boolean isExternal;

    /**
     * 是否缓存
     */
    private Boolean isCache;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private StatusEnum status;
}
