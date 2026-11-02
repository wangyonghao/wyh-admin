
package top.wyhao.system.admin.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色和部门关联实体
 *

 * @since 2023/2/18 21:57
 */
@Data
@NoArgsConstructor
public class RoleDepartment {

    private Long id;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 部门 ID
     */
    private Long departmentId;

    public RoleDepartment(Long roleId, Long departmentId) {
        this.roleId = roleId;
        this.departmentId = departmentId;
    }
}
