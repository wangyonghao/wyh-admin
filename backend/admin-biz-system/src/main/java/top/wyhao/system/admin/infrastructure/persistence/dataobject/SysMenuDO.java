
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.system.admin.domain.model.MenuType;
import top.wyhao.mybatis.model.BaseEntity;
import top.wyhao.cmn.core.enums.StatusEnum;

/**
 * 菜单实体
 *

 * @since 2023/2/15 20:14
 */
@Data
@TableName("sys_menu")
public class SysMenuDO extends BaseEntity {
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
