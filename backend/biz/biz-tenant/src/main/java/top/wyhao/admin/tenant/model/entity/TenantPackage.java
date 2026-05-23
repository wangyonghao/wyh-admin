
package top.wyhao.admin.tenant.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.cmn.db.model.BaseEntity;
import top.wyhao.starter.core.enums.StatusEnum;

/**
 * 套餐实体
 *


 * @since 2024/11/26 11:25
 */
@Data
@TableName("tenant_package")
public class TenantPackage extends BaseEntity {
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