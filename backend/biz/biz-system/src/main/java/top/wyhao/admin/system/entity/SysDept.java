
package top.wyhao.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.cmn.db.model.BaseEntity;
import top.wyhao.starter.core.enums.StatusEnum;

/**
 * 部门实体
 *

 * @since 2023/1/22 13:50
 */
@Data
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    /**
     * 名称
     */
    private String name;

    /**
     * 上级部门 ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private StatusEnum status;

    /**
     * 是否为系统内置数据
     */
    private Boolean isBuiltin;
}
