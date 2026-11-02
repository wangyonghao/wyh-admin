
package top.wyhao.system.admin.domain.model;

import java.time.LocalDateTime;


import lombok.Data;
import top.wyhao.cmn.core.enums.StatusEnum;

/**
 * 套餐实体
 *

 * @since 2024/11/26 11:25
 */
@Data
public class TenantPackage {

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
     * 名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 菜单选择是否父子节点关联
     */
    private Boolean menuCheckStrictly;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private StatusEnum status;
}