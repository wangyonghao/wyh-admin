
package top.wyhao.system.admin.domain.model;

import java.time.LocalDateTime;


import lombok.Data;
import top.wyhao.system.admin.domain.model.MenuType;
import top.wyhao.cmn.core.enums.StatusEnum;

/**
 * 菜单实体
 *

 * @since 2023/2/15 20:14
 */
@Data
public class Menu {

    private Long id;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Long updateUser;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

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

    /**
     * 稳定标识，创建后不可修改
     */
    private String code;

    /**
     * PLATFORM 或 TENANT
     */
    private String scope;

    /**
     * 系统保护，不能停用或删除
     */
    private Boolean isSystem;

    /**
     * 必含，不能停用或删除
     */
    private Boolean isRequired;

    /**
     * 软删除时间
     */
    private LocalDateTime deletedAt;
}
