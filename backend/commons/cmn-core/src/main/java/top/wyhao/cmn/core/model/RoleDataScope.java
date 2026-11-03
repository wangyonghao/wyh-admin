
package top.wyhao.cmn.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import top.wyhao.cmn.core.enums.DataScopeEnum;

import java.util.List;

/**
 * 角色上下文
 *

 * @since 2023/3/7 22:08
 */
@Data
@NoArgsConstructor
public class RoleDataScope {
    private Long id;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 数据权限
     */
    private DataScopeEnum dataScope;

    private List<Long> visibleDepartmentIds;
}
