package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.RoleDepartment;

import java.util.Collection;
import java.util.List;

/**
 * 角色与部门关联仓储。
 */
public interface RoleDepartmentRepository {

    List<Long> listDepartmentIdsByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);

    void deleteByDepartmentIds(List<Long> departmentIds);

    boolean insertBatch(List<RoleDepartment> roleDepartments);

    /**
     * 覆盖式保存角色部门关联；无变更返回 false。
     */
    boolean replaceByRoleId(Long roleId, List<Long> departmentIds);

    void deleteAll();

    boolean deleteByRoleIdNotIn(Collection<Long> keepRoleIds);
}
