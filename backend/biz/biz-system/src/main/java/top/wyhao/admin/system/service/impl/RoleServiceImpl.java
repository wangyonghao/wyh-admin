
package top.wyhao.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.entity.SysMenu;
import top.wyhao.admin.system.entity.SysRole;
import top.wyhao.admin.system.entity.SysUserRole;
import top.wyhao.admin.system.entity.SysUser;
import top.wyhao.admin.system.mapper.SysMenuMapper;
import top.wyhao.admin.system.mapper.SysRoleMapper;
import top.wyhao.admin.system.mapper.SysUserRoleMapper;
import top.wyhao.admin.system.mapper.SysUserMapper;
import top.wyhao.admin.system.model.bo.RolePermissionUpdateRequest;
import top.wyhao.admin.system.model.bo.RoleRequest;
import top.wyhao.admin.system.model.query.RoleQuery;
import top.wyhao.admin.system.model.query.RoleUserQuery;
import top.wyhao.admin.system.model.vo.MenuVO;
import top.wyhao.admin.system.model.vo.role.RoleDetailResult;
import top.wyhao.admin.system.model.vo.role.RoleResult;
import top.wyhao.admin.system.model.vo.role.RoleUserResult;
import top.wyhao.admin.system.service.RoleDeptService;
import top.wyhao.admin.system.service.RoleMenuService;
import top.wyhao.admin.system.service.RoleService;
import top.wyhao.cmn.db.util.QueryWrapperUtil;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.core.enums.DataScopeEnum;
import top.wyhao.starter.core.enums.RoleCodeEnum;
import top.wyhao.starter.core.exception.BadRequestException;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色 Service
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {
    /**
     * 超级管理员角色 ID（内置且仅有一位超级管理员用户）
     */
    public static final Long SUPERADMIN_ROLE_ID = 1L;

    private final RoleMenuService roleMenuService;
    private final RoleDeptService roleDeptService;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public PageResult<RoleResult> page(RoleQuery query, PageQuery pageQuery) {
        QueryWrapper<SysRole> wrapper = QueryWrapperUtil.build(query, query.getSort());
        IPage<SysRole> page = roleMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), wrapper);
        return PageResult.build(page, RoleResult.class);
    }

    @Override
    public List<RoleResult> list(RoleQuery query, SortQuery sortQuery) {
        QueryWrapper<SysRole> wrapper = QueryWrapperUtil.build(query, sortQuery.getSort());
        List<SysRole> entities = roleMapper.selectList(wrapper);
        return entities.stream()
                .map(this::convertToRoleResp)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDetailResult detail(Long id) {
        SysRole entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("ROLE_NOT_FOUND", "角色不存在");
        }
        RoleDetailResult detail = convertToRoleDetailResp(entity);
        detail.setMenuIds(roleMenuService.listMenuIdByRoleIds(List.of(detail.getId())));
        detail.setDeptIds(roleDeptService.listDeptIdByRoleId(detail.getId()));
        return detail;
    }

    @Override
    public Long create(RoleRequest req) {
        this.checkNameExists(req.getName(), null);
        String code = req.getCode();
        // 防止租户添加超级管理员
        BizAssert.throwIfEqual(RoleCodeEnum.SUPER_ADMIN.getCode(), req.getCode(), "编码 [{}] 禁止使用", code);
        // 新增信息
        SysRole entity = new SysRole();
        updateEntityFromReq(entity, req);
        int result = roleMapper.insert(entity);
        if (result <= 0) {
            throw new BadRequestException("CREATE_FAILED", "创建失败");
        }
        // 保存角色和部门关联
        roleDeptService.add(req.getDeptIds(), entity.getId());
        return entity.getId();
    }

    @Override
    public void update(RoleRequest req, Long id) {
        this.checkNameExists(req.getName(), id);
        SysRole oldRole = roleMapper.selectById(id);
        BizAssert.throwIfNotEqual(req.getCode(), oldRole.getCode(), "角色编码不允许修改", oldRole.getName());
        DataScopeEnum oldDataScope = oldRole.getDataScope();
        if (Boolean.TRUE.equals(oldRole.getIsBuiltin())) {
            BizAssert.throwIfNotEqual(req.getDataScope(), oldDataScope, "[{}] 是系统内置角色，不允许修改角色数据权限", oldRole.getName());
        }
        // 更新信息
        SysRole entity = roleMapper.selectById(id);
        updateEntityFromReq(entity, req);
        int result = roleMapper.updateById(entity);
        if (result <= 0) {
            throw new BadRequestException("UPDATE_FAILED", "更新失败");
        }
        if (RoleCodeEnum.isSuperRoleCode(req.getCode())) {
            return;
        }
        // 保存角色和部门关联
        boolean isSaveDeptSuccess = roleDeptService.add(req.getDeptIds(), id);
        // 如果数据权限有变更，则更新在线用户权限信息
        if (isSaveDeptSuccess || ObjectUtil.notEqual(req.getDataScope(), oldDataScope)) {
            this.updateUserContext(id);
        }
    }

    private void checkNameExists(String name, Long id) {
        if (menuMapper.isNameExists(name, id)) {
            throw new BadRequestException("ROLENAME_ALREADY_EXISTS", "角色名称 [" + name + "] 已存在");
        }
    }

    @Override
    public void delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("ROLE_NOT_FOUND", "角色不存在");
        }
        if (role.getIsBuiltin()) {
            throw new BusinessException("ROLE_NOT_ALLOWED_DELETE", StrUtil.format("所选角色 [{}] 是系统内置角色，不允许删除", role.getName()));
        }
        if (this.hasMember(id)) {
            throw new BusinessException("ROLE_NOT_ALLOWED_DELETE", "所选角色存在用户关联，请解除关联后重试");
        }

        // 删除角色和菜单关联
        roleMenuService.deleteByRoleId(id);
        // 删除角色和部门关联
        roleDeptService.deleteByRoleId(id);
        // 删除角色
        roleMapper.deleteById(id);
    }


    @Override
    public void export(RoleQuery query, SortQuery sortQuery, HttpServletResponse response) {
        // 实现导出逻辑
        List<RoleResult> list = list(query, sortQuery);
        // 使用Excel工具导出数据到response
        ExcelUtils.export(list, "角色数据", RoleResult.class, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(key = "#roleId", name = CacheConstants.ROLE_MENU_KEY_PREFIX)
    public void updatePermission(Long roleId, RolePermissionUpdateRequest req) {
        SysRole role = roleMapper.selectById(roleId);
        BizAssert.isTrue(role.getIsBuiltin(), "[{}] 是系统内置角色，不允许修改角色功能权限", role.getName());
        // 保存角色和菜单关联
        roleMenuService.save(req.getMenuIds(), roleId);
        roleMapper.lambdaUpdate()
                .set(SysRole::getMenuCheckStrictly, req.getMenuCheckStrictly())
                .eq(SysRole::getId, roleId)
                .update();
    }

    @Override
    public void assignToUsers(Long roleId, List<Long> userIds) {
        SysRole role = roleMapper.selectById(roleId);
        BizAssert.isTrue(Boolean.TRUE.equals(role.getIsBuiltin()), "[{}] 是系统内置角色，不允许分配角色给其他用户", role.getName());
        // 保存用户和角色关联
        this.assignRoleToUsers(roleId, userIds);
        // 更新用户上下文
        this.updateUserContext(roleId);
    }

    private void assignRoleToUsers(Long roleId, List<Long> userIds) {
        List<SysUserRole> userRoleList = CollUtils.mapToList(userIds, userId -> new SysUserRole(userId, roleId));
        userRoleMapper.insertBatch(userRoleList);
    }

    @Override
    public void updateUserContext(Long roleId) {
        List<Long> userIdList = this.listMemberIds(roleId);
        // 更新登录用户的权限信息
    }

    @Override
    public Long getIdByCode(String code) {
        return roleMapper.lambdaQuery().eq(SysRole::getCode, code).oneOpt().map(SysRole::getId).orElse(null);
    }

    @Override
    public List<SysRole> listByNames(List<String> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return roleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getName, list));
    }

    @Override
    public int countByNames(List<String> roleNames) {
        if (CollUtil.isEmpty(roleNames)) {
            return 0;
        }
        return roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery().in(SysRole::getName, roleNames)).intValue();
    }

    private boolean hasMember(Long roleId) {
        return userRoleMapper.lambdaQuery().eq(SysUserRole::getRoleId, roleId).exists();
    }

    private void fill(Object obj) {
        if (obj instanceof RoleDetailResult detail) {
            Long roleId = detail.getId();
            List<MenuVO> list = this.listMenuByRoleId(roleId);
            List<Long> menuIds = CollUtils.mapToList(list, MenuVO::getId);
            detail.setMenuIds(menuIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(List<Long> newRoleIds, Long userId) {
        SysUser userDO = userMapper.selectById(userId);
        // 检查是否有变更
        List<Long> oldRoleIds = userRoleMapper.lambdaQuery()
                .select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, userId)
                .list()
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (CollUtil.isEmpty(CollUtil.disjunction(newRoleIds, oldRoleIds))) {
            return false;
        }
        // 删除原有关联
        userRoleMapper.lambdaUpdate().eq(SysUserRole::getUserId, userId).remove();
        // 保存最新关联
        List<SysUserRole> userRoleList = CollUtils.mapToList(newRoleIds, roleId -> new SysUserRole(userId, roleId));
        return userRoleMapper.insertBatch(userRoleList);
    }

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        return userRoleMapper.lambdaQuery()
                .select(SysUserRole::getRoleId)
                .eq(SysUserRole::getUserId, userId)
                .list()
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }



    @Override
    @Cached(key = "#roleId", name = CacheConstants.ROLE_MENU_KEY_PREFIX)
    public List<MenuVO> listMenuByRoleId(Long roleId) {
        List<SysMenu> menuList;
        if (SUPERADMIN_ROLE_ID.equals(roleId)) {
            menuList = menuMapper.lambdaQuery().eq(SysMenu::getStatus, "1").list();
        } else {
            menuList = menuMapper.selectListByRoleId(roleId);
        }
        List<MenuVO> list = BeanUtil.copyToList(menuList, MenuVO.class);
        list.forEach(this::fill);
        return list;
    }
    @Override
    public List<Long> listMemberIds(Long roleId) {
        return userRoleMapper.lambdaQuery()
                .select(SysUserRole::getUserId)
                .eq(SysUserRole::getRoleId, roleId)
                .list()
                .stream()
                .map(SysUserRole::getUserId)
                .toList();
    }

    @Override
    public List<RoleUserResult> pageMember(Long roleId, RoleUserQuery query) {
        QueryWrapper<SysUserRole> wrapper = Wrappers.query();
        wrapper.eq("role_id", roleId)
                .and(StrUtil.isNotBlank(query.getKeyword()),
                        w -> w.like("su.username", query.getKeyword())
                                .or().like("su.nickname", query.getKeyword()));
        IPage<SysUserRole> page = new Page<>(query.getPage(), query.getSize());
        return userRoleMapper.selectUserPage(page, wrapper).getRecords();
    }

    @Override
    public void deleteMember(Long roleId, List<Long> userIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("ROLE_NOT_FOUND", "角色不存在");
        }
        userRoleMapper.lambdaUpdate().eq(SysUserRole::getRoleId, roleId).in(SysUserRole::getUserId, userIds).remove();
    }

    private RoleResult convertToRoleResp(SysRole entity) {
        RoleResult resp = new RoleResult();
        resp.setId(entity.getId());
        resp.setName(entity.getName());
        resp.setCode(entity.getCode());
        resp.setDescription(entity.getDescription());
        resp.setIsBuiltin(entity.getIsBuiltin());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateTime(entity.getUpdateTime());
        return resp;
    }

    private RoleDetailResult convertToRoleDetailResp(SysRole entity) {
        RoleDetailResult resp = new RoleDetailResult();
        resp.setId(entity.getId());
        resp.setName(entity.getName());
        resp.setCode(entity.getCode());
        resp.setDescription(entity.getDescription());
        resp.setDataScope(entity.getDataScope());
        resp.setMenuCheckStrictly(entity.getMenuCheckStrictly());
        resp.setIsBuiltin(entity.getIsBuiltin());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateTime(entity.getUpdateTime());
        return resp;
    }

    private void updateEntityFromReq(SysRole entity, RoleRequest req) {
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setDescription(req.getDescription());
        entity.setDataScope(req.getDataScope());
        if (entity.getId() == null) { // 创建时设置
            entity.setIsBuiltin(false);
            entity.setCreateTime(LocalDateTime.now());
        }
        entity.setUpdateTime(LocalDateTime.now());
    }
}