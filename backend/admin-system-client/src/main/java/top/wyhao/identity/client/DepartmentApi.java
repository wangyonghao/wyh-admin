package top.wyhao.identity.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门业务 API（Admin 对外提供）
 */
public interface DepartmentApi {

    List<Long> listSelfAndDescendantIds(Long departmentId);

    int countValidDepartmentPaths(Collection<String> departmentPaths);

    Map<String, Long> resolveDepartmentIdsByPaths(Collection<String> departmentPaths);

    boolean isDisabled(Long departmentId);
}
