
package top.wyhao.admin.system.provider;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.entity.SysDept;
import top.wyhao.admin.system.entity.SysRole;
import top.wyhao.admin.system.entity.SysUser;
import top.wyhao.admin.system.mapper.*;
import top.wyhao.admin.system.mapper.SysUserMapper;
import top.wyhao.admin.system.mapper.SysUserPasswordHistoryMapper;
import top.wyhao.admin.system.mapper.SysUserSocialMapper;
import top.wyhao.admin.system.service.FileService;
import top.wyhao.admin.system.service.RoleMenuService;
import top.wyhao.admin.system.service.RoleService;
import top.wyhao.starter.core.constant.GlobalConstants;
import top.wyhao.starter.core.enums.DataScopeEnum;
import top.wyhao.starter.core.enums.GenderEnum;
import top.wyhao.starter.core.enums.RoleCodeEnum;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.model.TenantBO;
import top.wyhao.starter.core.spi.PackageMenuApi;
import top.wyhao.starter.core.spi.TenantApi;
import top.wyhao.starter.core.spi.TenantDataApi;
import top.wyhao.starter.core.util.ExceptionUtils;
import top.wyhao.starter.core.util.RsaUtils;
import top.wyhao.starter.core.util.validation.ValidationUtils;
import top.wyhao.starter.tenant.util.TenantUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户数据 API 实现
 *


 * @since 2024/12/2 20:12
 */
@Service
@RequiredArgsConstructor
public class TenantDataApiForSystemImpl implements TenantDataApi {

    private final PackageMenuApi packageMenuApi;
    private final TenantApi tenantApi;
    private final FileService fileService;
    private final RoleMenuService roleMenuService;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysOperationLogMapper operationLogMapper;
    private final SysMessageMapper messageMapper;
    private final SysMessageMapper messageUserMapper;
    private final SysNoticeMapper noticeMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserMapper userMapper;
    private final SysUserPasswordHistoryMapper userPasswordHistoryMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserSocialMapper userSocialMapper;
    private final RoleService roleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void init(TenantBO tenant) {
        Long tenantId = tenant.getId();
        TenantUtils.execute(tenantId, () -> {
            // 初始化部门
            Long deptId = this.initDeptData(tenant);
            // 初始化角色
            Long roleId = this.initRoleData(tenant);
            // 角色绑定菜单
            List<Long> menuIds = packageMenuApi.listMenuIdsByPackageId(tenant.getPackageId());
            roleMenuService.save(menuIds, roleId);
            // 初始化管理用户
            Long userId = this.initUserData(tenant, deptId);
            // 用户绑定角色
            roleService.assignToUsers(roleId, ListUtil.of(userId));
            // 租户绑定用户
            tenantApi.bindAdminUser(tenantId, userId);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        // 退出所有用户
        List<SysUser> userList = userMapper.selectList(null);
        for (SysUser user : userList) {
            StpUtil.logout(user.getId());
        }
        Wrapper queryWrapper = Wrappers.query().eq("1", 1);
        // 部门清除
        deptMapper.delete(queryWrapper);
//        // 文件清除
//        List<Long> fileIds = CollUtils.mapToList(fileService.list(), FileDO::getId);
//        if (!fileIds.isEmpty()) {
//            fileService.delete(fileIds);
//        }
        // 日志清除
        operationLogMapper.delete(queryWrapper);
        // 消息清除
        messageMapper.delete(queryWrapper);
        messageUserMapper.delete(queryWrapper);
        // 通知清除
        noticeMapper.delete(queryWrapper);
        // 角色相关数据清除
        roleMapper.delete(queryWrapper);
        roleDeptMapper.delete(queryWrapper);
        roleMenuMapper.delete(queryWrapper);
        // 用户数据清除
        userMapper.delete(queryWrapper);
        userPasswordHistoryMapper.delete(queryWrapper);
        userRoleMapper.delete(queryWrapper);
        userSocialMapper.delete(queryWrapper);
    }

    /**
     * 初始化部门数据
     *
     * @param tenant 租户信息
     * @return 部门 ID
     */
    private Long initDeptData(TenantBO tenant) {
        SysDept dept = new SysDept();
        dept.setName(tenant.getName());
        dept.setParentId(GlobalConstants.ROOT_PARENT_ID);
        dept.setAncestors(GlobalConstants.ROOT_PARENT_ID.toString());
        dept.setDescription("系统初始部门");
        dept.setSort(1);
        dept.setStatus(StatusEnum.ENABLE);
        dept.setIsBuiltin(true);
        deptMapper.insert(dept);
        return dept.getId();
    }

    /**
     * 初始化角色数据
     *
     * @param tenant 租户信息
     * @return 角色 ID
     */
    private Long initRoleData(TenantBO tenant) {
        SysRole role = new SysRole();
        RoleCodeEnum tenantAdmin = RoleCodeEnum.TENANT_ADMIN;
        role.setName(tenantAdmin.getDescription());
        role.setCode(tenantAdmin.getCode());
        role.setDataScope(DataScopeEnum.ALL);
        role.setDescription("系统初始角色");
        role.setSort(1);
        role.setIsBuiltin(true);
        role.setMenuCheckStrictly(true);
        role.setDeptCheckStrictly(true);
        roleMapper.insert(role);
        return role.getId();
    }

    /**
     * 初始化用户数据
     *
     * @param tenant 租户信息
     * @param deptId 部门 ID
     * @return 用户 ID
     */
    private Long initUserData(TenantBO tenant, Long deptId) {
        // 解密密码
        String rawPassword = ExceptionUtils.exToNull(() -> RsaUtils.decryptByRsaPrivateKey(tenant.getAdminPassword()));
        ValidationUtils.throwIfBlank(rawPassword, "密码解密失败");
        // 初始化用户
        SysUser user = new SysUser();
        user.setUsername(tenant.getAdminUsername());
        user.setNickname(RoleCodeEnum.TENANT_ADMIN.getDescription());
        user.setPassword(rawPassword);
        user.setGender(GenderEnum.UNKNOWN.getValue());
        user.setDescription("系统初始用户");
        user.setStatus(StatusEnum.ENABLE.getValue());
        user.setIsBuiltin(true);
        user.setPwdUpdateTime(LocalDateTime.now());
        user.setDeptId(deptId);
        userMapper.insert(user);
        return user.getId();
    }
}
