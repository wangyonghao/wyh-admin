
package top.wyhao.system.admin.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.wyhao.mybatis.model.BaseEntity;
import top.wyhao.tenant.core.TenantedEntity;

/**
 * 部门实体
 *
 * @since 2023/1/22 13:50
 */
@Data
@TableName("sys_department")
public class SysDepartmentDO extends BaseEntity implements TenantedEntity {
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 部门类型
     * 例如：分公司、部门、工作组
     */
    private Integer type;
    /**
     * 上级部门 ID
     */
    private Long parentId;
    /**
     * 祖先ID路径（以逗号分隔）
     * 例：0,1001,1002 快速查询上级或下级部门
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
    private Integer status;
    /**
     * 是否为系统内置数据
     */
    private Boolean isBuiltin;
    /**
     * 租户 ID。行级隔离列，与当前登录租户一致。
     */
    private Long tenantId;
}
