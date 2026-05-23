
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.entity.SysRoleDept;
import top.wyhao.admin.system.mapper.SysRoleDeptMapper;
import top.wyhao.admin.system.service.RoleDeptService;
import top.wyhao.starter.core.util.CollUtils;

import java.util.List;

/**
 * 角色和部门关联业务实现
 *

 * @since 2023/2/19 10:47
 */
@Service
@RequiredArgsConstructor
public class RoleDeptServiceImpl implements RoleDeptService {

    private final SysRoleDeptMapper roleDeptMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(List<Long> deptIds, Long roleId) {
        // 检查是否有变更
        List<Long> oldDeptIdList = roleDeptMapper.lambdaQuery()
            .select(SysRoleDept::getDeptId)
            .eq(SysRoleDept::getRoleId, roleId)
            .list()
            .stream()
            .map(SysRoleDept::getDeptId)
            .toList();
        if (CollUtil.isEmpty(CollUtil.disjunction(deptIds, oldDeptIdList))) {
            return false;
        }
        // 删除原有关联
        roleDeptMapper.lambdaUpdate().eq(SysRoleDept::getRoleId, roleId).remove();
        // 保存最新关联
        List<SysRoleDept> roleDeptList = CollUtils.mapToList(deptIds, deptId -> new SysRoleDept(roleId, deptId));
        return roleDeptMapper.insertBatch(roleDeptList);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
            roleDeptMapper.lambdaUpdate().in(SysRoleDept::getRoleId, roleId).remove();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDeptIds(List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return;
        }
        roleDeptMapper.lambdaUpdate().in(SysRoleDept::getDeptId, deptIds).remove();
    }

    @Override
    public List<Long> listDeptIdByRoleId(Long roleId) {
        return roleDeptMapper.selectDeptIdByRoleId(roleId);
    }
}
