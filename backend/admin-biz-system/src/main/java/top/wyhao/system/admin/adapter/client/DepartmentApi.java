package top.wyhao.system.admin.adapter.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门业务 API（部门域对外提供）
 */
public interface DepartmentApi {

    /**
     * 查询部门自身及全部下级 ID
     */
    List<Long> listSelfAndDescendantIds(Long departmentId);

    /**
     * 校验部门路径是否均有效，返回有效数量
     */
    int countValidDepartmentPaths(Collection<String> departmentPaths);

    /**
     * 将部门路径解析为 ID 映射
     */
    Map<String, Long> resolveDepartmentIdsByPaths(Collection<String> departmentPaths);

    /**
     * 部门是否禁用
     */
    boolean isDisabled(Long departmentId);
}
