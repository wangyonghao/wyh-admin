package top.wyhao.system.admin.app.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.cmn.core.enums.DataScopeEnum;
import top.wyhao.cmn.core.enums.GenderEnum;
import top.wyhao.cmn.core.enums.RoleCodeEnum;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.cmn.core.util.ExceptionUtils;
import top.wyhao.cmn.core.util.RsaUtils;
import top.wyhao.system.admin.app.service.MembershipService;
import top.wyhao.system.admin.app.service.TenantDataAppService;
import top.wyhao.system.admin.domain.PackageMenuBinding;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.DepartmentRepository;
import top.wyhao.system.admin.domain.gateway.RoleDepartmentRepository;
import top.wyhao.system.admin.domain.gateway.RoleMenuRepository;
import top.wyhao.system.admin.domain.gateway.RoleRepository;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.gateway.UserRoleRepository;
import top.wyhao.system.admin.domain.gateway.UserSocialRepository;
import top.wyhao.system.admin.domain.model.Department;
import top.wyhao.system.admin.domain.model.Role;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.system.admin.domain.model.UserRole;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysMessageMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysNoticeMapper;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysOperationLogMapper;
import top.wyhao.tenant.client.PackageMenuApi;
import top.wyhao.tenant.client.TenantApi;
import top.wyhao.tenant.client.TenantBO;
import top.wyhao.tenant.util.TenantUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户数据初始化/清理编排实现。
 * <p>
 * notification / audit 清理由 service 层直连 Mapper（文档允许的经典分层暂留）。
 */
@Service
@RequiredArgsConstructor
public class TenantDataAppServiceImpl implements TenantDataAppService {

    public static final Long ROOT_PARENT_ID = 0L;

    private final PackageMenuApi packageMenuApi;
    private final TenantApi tenantApi;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final SysOperationLogMapper operationLogMapper;
    private final SysMessageMapper messageMapper;
    private final SysNoticeMapper noticeMapper;
    private final RoleDepartmentRepository roleDepartmentRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserSocialRepository userSocialRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipService membershipService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void init(TenantBO tenant) {
        Long tenantId = tenant.getId();
        TenantUtils.execute(tenantId, () -> {
            Long departmentId = this.initDepartmentData(tenant);
            Long roleId = this.initRoleData(tenant);
            List<Long> menuIds = PackageMenuBinding.menusForTenantAdmin(
                packageMenuApi.listMenuIdsByPackageId(tenant.getPackageId()));
            roleMenuRepository.replaceByRoleId(roleId, menuIds);
            Long userId;
            if (tenant.getAdminUserId() != null) {
                userId = tenant.getAdminUserId();
            } else {
                userId = this.initUserData(tenant, departmentId);
            }
            // 租户管理员角色是内置角色：UI 侧 assignToUsers 会拒绝分配，初始化必须直写关联。
            userRoleRepository.insertBatch(ListUtil.of(new UserRole(userId, roleId)));
            membershipService.addRoleMembers(roleId, ListUtil.of(userId));
            tenantApi.bindAdminUser(tenantId, userId);
            membershipService.ensureTenantMembership(userId, tenantId, true);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        List<User> userList = userRepository.listAll();
        for (User user : userList) {
            StpUtil.logout(user.getId());
        }
        Wrapper queryWrapper = Wrappers.query().eq("1", 1);
        departmentRepository.deleteAll();
        operationLogMapper.delete(queryWrapper);
        messageMapper.delete(queryWrapper);
        noticeMapper.delete(queryWrapper);
        roleRepository.deleteAll();
        roleDepartmentRepository.deleteAll();
        roleMenuRepository.deleteAll();
        userRepository.deleteAll();
        userRoleRepository.deleteAll();
        userSocialRepository.deleteAll();
    }

    private Long initDepartmentData(TenantBO tenant) {
        Department department = new Department();
        department.setName(tenant.getName());
        department.setCode("000000"); // todo
        department.setType(1); // todo
        department.setParentId(ROOT_PARENT_ID);
        department.setAncestors(ROOT_PARENT_ID.toString());
        department.setDescription("系统初始部门");
        department.setSort(1);
        department.setStatus(StatusEnum.ENABLE.getValue());
        department.setIsBuiltin(true);
        department.setTenantId(tenant.getId());
        departmentRepository.insert(department);
        return department.getId();
    }

    private Long initRoleData(TenantBO tenant) {
        Role role = new Role();
        RoleCodeEnum tenantAdmin = RoleCodeEnum.TENANT_ADMIN;
        role.setName(tenantAdmin.getDescription());
        role.setCode(tenantAdmin.getCode());
        role.setDataScope(DataScopeEnum.ALL);
        role.setDescription("系统初始角色");
        role.setSort(1);
        role.setIsBuiltin(true);
        role.setMenuCheckStrictly(true);
        role.setDepartmentCheckStrictly(true);
        role.setTenantId(tenant.getId());
        roleRepository.insert(role);
        return role.getId();
    }

    private Long initUserData(TenantBO tenant, Long departmentId) {
        String rawPassword = ExceptionUtils.exToNull(() -> RsaUtils.decryptByRsaPrivateKey(tenant.getAdminPassword()));
        if (CharSequenceUtil.isBlank(rawPassword)) {
            throw UserException.passwordDecryptFailed();
        }
        User user = new User();
        user.setUsername(tenant.getAdminUsername());
        user.setDisplayName(RoleCodeEnum.TENANT_ADMIN.getDescription());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setGender(GenderEnum.UNKNOWN.getValue());
        user.setDescription("系统初始用户");
        user.setStatus(StatusEnum.ENABLE.getValue());
        user.setIsBuiltin(true);
        user.setPwdUpdateTime(LocalDateTime.now());
        user.setDepartmentId(departmentId);
        user.setTenantId(tenant.getId());
        userRepository.insert(user);
        return user.getId();
    }
}
