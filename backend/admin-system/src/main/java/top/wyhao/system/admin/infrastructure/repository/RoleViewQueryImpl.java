package top.wyhao.system.admin.infrastructure.repository;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysUserRoleDO;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.SysRoleDO;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.system.admin.app.dto.RoleMemberQuery;
import top.wyhao.system.admin.app.dto.RoleQuery;
import top.wyhao.system.admin.app.dto.RoleMemberResult;
import top.wyhao.system.admin.app.query.RoleViewQuery;
import top.wyhao.system.admin.domain.model.Role;
import top.wyhao.system.admin.domain.model.UserRole;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysRoleMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysUserRoleMapper;

import java.util.List;

/**
 * 角色列表读模型实现。
 */
@Repository
@RequiredArgsConstructor
public class RoleViewQueryImpl implements RoleViewQuery {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public PageResult<Role> page(RoleQuery query, long page, long pageSize) {
        return PageResult.of(roleMapper.selectPage(new Page<>(page, pageSize), QueryWrapperBuilder.build(query, SysRoleDO.class)))
                .map(item -> AdminDoMapper.toDomain(item, Role.class));
    }

    @Override
    public List<Role> list(RoleQuery query) {
        return AdminDoMapper.toDomainList(roleMapper.selectList(QueryWrapperBuilder.build(query, SysRoleDO.class)), Role.class);
    }

    @Override
    public List<RoleMemberResult> pageMembers(Long roleId, RoleMemberQuery query, long page, long pageSize) {
        QueryWrapper<SysUserRoleDO> wrapper = Wrappers.query();
        wrapper.eq("role_id", roleId)
                .and(StrUtil.isNotBlank(query.getKeyword()),
                        w -> w.like("su.username", query.getKeyword())
                                .or().like("su.display_name", query.getKeyword()));
        IPage<SysUserRoleDO> memberPage = new Page<>(page, pageSize);
        return userRoleMapper.selectUserPage(memberPage, wrapper).getRecords();
    }
}
