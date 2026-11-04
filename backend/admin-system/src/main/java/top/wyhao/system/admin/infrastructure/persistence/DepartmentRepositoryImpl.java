package top.wyhao.system.admin.infrastructure.persistence;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.system.admin.domain.gateway.DepartmentRepository;
import top.wyhao.system.admin.domain.model.Department;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysDepartmentDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysDepartmentMapper;

import java.util.List;

/**
 * 部门仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final SysDepartmentMapper departmentMapper;

    @Override
    public Department findById(Long id) {
        return AdminDoMapper.toDomain(departmentMapper.selectById(id), Department.class);
    }

    @Override
    public void insert(Department department) {
        SysDepartmentDO data = AdminDoMapper.toDo(department, SysDepartmentDO.class);
        departmentMapper.insert(data);
        AdminDoMapper.copyBack(data, department);
    }

    @Override
    public void updateById(Department department) {
        SysDepartmentDO data = AdminDoMapper.toDo(department, SysDepartmentDO.class);
        departmentMapper.updateById(data);
        AdminDoMapper.copyBack(data, department);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        departmentMapper.deleteByIds(ids);
    }

    @Override
    public List<Department> listByQuery(Object query) {
        return AdminDoMapper.toDomainList(
                departmentMapper.selectList(QueryWrapperBuilder.build(query, SysDepartmentDO.class)),
                Department.class);
    }

    @Override
    public List<Department> listChildren(Long id) {
        return AdminDoMapper.toDomainList(departmentMapper.listChildren(id), Department.class);
    }

    @Override
    public boolean existsName(String name, Long parentId, Long excludeId) {
        return departmentMapper.lambdaQuery()
                .eq(SysDepartmentDO::getName, name)
                .eq(SysDepartmentDO::getParentId, parentId)
                .ne(excludeId != null, SysDepartmentDO::getId, excludeId)
                .exists();
    }

    @Override
    public List<Department> listByName(String name) {
        return AdminDoMapper.toDomainList(
                departmentMapper.lambdaQuery().eq(SysDepartmentDO::getName, name).list(),
                Department.class);
    }

    @Override
    public Department findByNameAndParentId(String name, Long parentId) {
        return AdminDoMapper.toDomain(
                departmentMapper.lambdaQuery()
                        .eq(SysDepartmentDO::getName, name)
                        .eq(SysDepartmentDO::getParentId, parentId)
                        .one(),
                Department.class);
    }

    @Override
    public long countChildren(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0L;
        }
        return ids.stream()
                .mapToLong(id -> departmentMapper.lambdaQuery()
                        .apply("(select position(',{0},' in ','||ancestors||',')) <> 0", id)
                        .count())
                .sum();
    }

    @Override
    public void updateChildrenAncestors(String newAncestors, String oldAncestors, Long parentId) {
        List<Department> children = listChildren(parentId);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        for (Department child : children) {
            Department update = new Department();
            update.setId(child.getId());
            update.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            updateById(update);
        }
    }

    @Override
    public long countIdGreaterThan(Long id) {
        return departmentMapper.lambdaQuery().gt(SysDepartmentDO::getId, id).count();
    }

    @Override
    public boolean deleteIdGreaterThan(Long id) {
        return departmentMapper.lambdaUpdate().gt(SysDepartmentDO::getId, id).remove();
    }

    @Override
    public void deleteAll() {
        departmentMapper.delete(Wrappers.<SysDepartmentDO>query().eq("1", 1));
    }
}
