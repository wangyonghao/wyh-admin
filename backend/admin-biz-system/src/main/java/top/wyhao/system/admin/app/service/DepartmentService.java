
package top.wyhao.system.admin.app.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wyhao.system.admin.domain.model.Department;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import top.wyhao.system.admin.app.dto.DepartmentQuery;
import top.wyhao.system.admin.app.dto.DepartmentRequest;
import top.wyhao.system.admin.app.dto.DepartmentResult;

/**
 * 部门业务接口
 */
public interface DepartmentService{

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @return 列表信息
     */
    List<DepartmentResult> list(DepartmentQuery query);

    /**
     * 查询部门树
     *
     * @param query     查询条件
     * @return 树列表信息
     */
    List<DepartmentResult> tree(DepartmentQuery query);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    DepartmentResult get(Long id);

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return 自增 ID
     */
    Long create(DepartmentRequest req);

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(DepartmentRequest req, Long id);

    /**
     * 删除
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 导出
     *
     * @param query     查询条件
     * @param response  响应对象
     */
    void export(DepartmentQuery query, HttpServletResponse response);

    /**
     * 查询子部门列表
     *
     * @param id ID
     * @return 子部门列表
     */
    List<Department> listChildren(Long id);

    Department getById(Long departmentId);

    /**
     * 按路径解析部门（单级名或多级 a/b/c）。
     */
    Department resolveByPath(String departmentPath);

    int countValidDepartmentPaths(Collection<String> departmentPaths);

    Map<String, Long> resolveDepartmentIdsByPaths(Collection<String> departmentPaths);
}

