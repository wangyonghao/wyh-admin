package top.wyhao.system.admin.infrastructure.repository;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.cmn.core.util.CollUtils;
import top.wyhao.system.admin.domain.gateway.RoleDepartmentRepository;
import top.wyhao.system.admin.domain.model.RoleDepartment;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleDepartmentDO;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysRoleDepartmentMapper;

import java.util.Collection;
import java.util.List;

/**
 * 角色与部门关联仓储实现。
 */
@Repository
@RequiredArgsConstructor
public class RoleDepartmentRepositoryImpl implements RoleDepartmentRepository {

    private final SysRoleDepartmentMapper roleDepartmentMapper;

    @Override
    public List<Long> listDepartmentIdsByRoleId(Long roleId) {
        return roleDepartmentMapper.selectDepartmentIdByRoleId(roleId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        roleDepartmentMapper.lambdaUpdate().eq(SysRoleDepartmentDO::getRoleId, roleId).remove();
    }

    @Override
    public void deleteByDepartmentIds(List<Long> departmentIds) {
        if (CollUtil.isEmpty(departmentIds)) {
            return;
        }
        roleDepartmentMapper.lambdaUpdate().in(SysRoleDepartmentDO::getDepartmentId, departmentIds).remove();
    }

    @Override
    public boolean insertBatch(List<RoleDepartment> roleDepartments) {
        return roleDepartmentMapper.insertBatch(AdminDoMapper.toDoList(roleDepartments, SysRoleDepartmentDO.class));
    }

    @Override
    public boolean replaceByRoleId(Long roleId, List<Long> departmentIds) {
        List<Long> ids = CollUtil.emptyIfNull(departmentIds);
        List<Long> oldIds = listDepartmentIdsByRoleId(roleId);
        if (CollUtil.isEmpty(CollUtil.disjunction(ids, oldIds))) {
            return false;
        }
        deleteByRoleId(roleId);
        if (CollUtil.isEmpty(ids)) {
            return true;
        }
        List<RoleDepartment> roleDepartments = CollUtils.mapToList(ids, departmentId -> new RoleDepartment(roleId, departmentId));
        return insertBatch(roleDepartments);
    }

    @Override
    public void deleteAll() {
        roleDepartmentMapper.delete(Wrappers.<SysRoleDepartmentDO>query().eq("1", 1));
    }

    @Override
    public boolean deleteByRoleIdNotIn(Collection<Long> keepRoleIds) {
        return roleDepartmentMapper.lambdaUpdate().notIn(SysRoleDepartmentDO::getRoleId, keepRoleIds).remove();
    }
}
