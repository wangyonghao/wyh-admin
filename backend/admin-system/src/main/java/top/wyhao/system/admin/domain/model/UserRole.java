
package top.wyhao.system.admin.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户和角色实体
 *

 * @since 2023/2/13 23:13
 */
@Data
@NoArgsConstructor
public class UserRole {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 角色 ID
     */
    private Long roleId;

    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}
