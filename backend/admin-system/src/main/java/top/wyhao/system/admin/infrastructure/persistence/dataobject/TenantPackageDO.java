
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.mybatis.model.BaseEntity;
import top.wyhao.cmn.core.enums.StatusEnum;

/**
 * 套餐实体
 *


 * @since 2024/11/26 11:25
 */
@Data
@TableName("tenant_package")
public class TenantPackageDO extends BaseEntity {
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

    /**
     * 稳定标识
     */
    private String code;

    /**
     * MAIN 或 ADDON
     */
    private String kind;

    /**
     * DRAFT、PUBLISHED、DELISTED
     */
    private String lifecycle;

    /**
     * 首次发布时间。有值后名称不可改。
     */
    private java.time.LocalDateTime publishedAt;

    /**
     * 下架时间
     */
    private java.time.LocalDateTime delistedAt;

    /**
     * 乐观锁
     */
    private Integer version;
}