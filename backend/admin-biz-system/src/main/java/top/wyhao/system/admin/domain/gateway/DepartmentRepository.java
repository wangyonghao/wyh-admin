package top.wyhao.system.admin.domain.gateway;

import top.wyhao.system.admin.domain.model.Department;

import java.util.List;

/**
 * 部门仓储接口
 */
public interface DepartmentRepository {

    /**
     * 根据ID查询
     *
     * @param id ID
     * @return 部门信息
     */
    Department findById(Long id);

    /**
     * 插入
     *
     * @param department 部门信息
     */
    void insert(Department department);

    /**
     * 更新
     *
     * @param department 部门信息
     */
    void updateById(Department department);

    /**
     * 批量删除
     *
     * @param ids ID列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 查询列表
     *
     * @param query 查询条件
     * @return 部门列表
     */
    List<Department> listByQuery(Object query);

    /**
     * 查询子部门列表
     *
     * @param id 部门ID
     * @return 子部门列表
     */
    List<Department> listChildren(Long id);

    /**
     * 检查名称是否存在
     *
     * @param name     名称
     * @param parentId 上级ID
     * @param excludeId 排除ID
     * @return 是否存在
     */
    boolean existsName(String name, Long parentId, Long excludeId);

    /**
     * 根据名称查询
     *
     * @param name 名称
     * @return 部门列表
     */
    List<Department> listByName(String name);

    /**
     * 根据名称和上级ID查询
     *
     * @param name     名称
     * @param parentId 上级ID
     * @return 部门信息
     */
    Department findByNameAndParentId(String name, Long parentId);

    /**
     * 统计子部门数量
     *
     * @param ids ID列表
     * @return 子部门数量
     */
    long countChildren(List<Long> ids);

    /**
     * 按前缀替换方式更新子部门祖级列表
     *
     * @param newAncestors 新祖级列表
     * @param oldAncestors 原祖级列表
     * @param parentId     父部门 ID
     */
    void updateChildrenAncestors(String newAncestors, String oldAncestors, Long parentId);

    /**
     * 统计 ID 大于指定值的部门数量
     */
    long countIdGreaterThan(Long id);

    /**
     * 删除 ID 大于指定值的部门
     *
     * @return 是否删除成功
     */
    boolean deleteIdGreaterThan(Long id);

    /**
     * 删除全部部门
     */
    void deleteAll();
}
