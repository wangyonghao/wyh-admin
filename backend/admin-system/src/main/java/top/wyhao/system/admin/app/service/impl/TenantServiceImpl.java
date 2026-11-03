
package top.wyhao.system.admin.app.service.impl;

import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageDO;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantDO;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.extra.spring.SpringUtil;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.provider.IdGeneratorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.admin.app.assembler.TenantAssembler;
import top.wyhao.tenant.exception.TenantException;
import top.wyhao.system.admin.infrastructure.repository.mapper.SysTenantMapper;
import top.wyhao.system.admin.domain.model.Tenant;
import top.wyhao.system.admin.app.dto.PackageQuery;
import top.wyhao.system.admin.domain.model.TenantPackage;
import top.wyhao.system.admin.infrastructure.repository.mapper.TenantPackageMapper;
import top.wyhao.system.admin.app.dto.TenantQuery;
import top.wyhao.system.admin.app.dto.TenantRequest;
import top.wyhao.system.admin.app.dto.PackageResp;
import top.wyhao.system.admin.app.dto.TenantDetailResp;
import top.wyhao.system.admin.app.dto.TenantResp;
import top.wyhao.system.admin.domain.PackageMenuBinding;
import top.wyhao.system.admin.domain.TenantLifecycleRules;
import top.wyhao.system.admin.app.service.PackageMenuService;
import top.wyhao.system.admin.app.service.PackageService;
import top.wyhao.system.admin.app.service.TenantService;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.redisson.util.RedisUtils;
import top.wyhao.cmn.core.constant.CacheConstants;
import top.wyhao.cmn.core.constant.StringConstants;
import top.wyhao.cmn.core.enums.RoleCodeEnum;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.security.client.RoleApi;
import top.wyhao.security.client.RoleMenuApi;
import top.wyhao.tenant.client.TenantBO;
import top.wyhao.tenant.client.TenantDataApi;
import top.wyhao.tenant.config.TenantProperties;
import top.wyhao.tenant.util.TenantUtils;
import top.wyhao.web.core.model.LabelValueResult;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租户业务实现
 *


 * @since 2024/11/26 17:20
 */
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    /**
     * 编码生成器 KEY
     */
    public static final String CODE_GENERATOR_KEY = "tenant-code";
    public static final String TENANT_KEY_PREFIX = "TENANT:";

    private final PackageService packageService;
    private final PackageMenuService packageMenuService;
    private final IdGeneratorProvider idGeneratorProvider;
    private final RoleMenuApi roleMenuApi;
    private final RoleApi roleApi;
    private final TenantProperties tenantProperties;
    private final SysTenantMapper baseMapper;
    private final TenantAssembler tenantAssembler;
    private final TenantPackageMapper tenantPackageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TenantRequest req) {
        this.checkNameRepeat(req.getName(), null);
        this.checkDomainRepeat(req.getDomain(), null);
        // 检查套餐状态
        packageService.checkStatus(req.getPackageId());
        // 生成租户编码
        req.setCode(this.generateCode());
        // 新增信息。请求可以不带状态，缺省启用，避免写入空状态。
        Tenant entity = tenantAssembler.toEntity(req);
        if (entity.getStatus() == null) {
            entity.setStatus(StatusEnum.ENABLE);
        }
        TenantDO __tenantDo = AdminDoMapper.toDo(entity, TenantDO.class);
        baseMapper.insert(__tenantDo);
        AdminDoMapper.copyBack(__tenantDo, entity);
        // 初始化租户数据。运行时再取 TenantDataApi，避免构造期 Spring 容器尚未注册完实现类。
        req.setId(entity.getId());
        tenantDataApis().forEach((key, value) -> value.init(tenantAssembler.toBO(req)));
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOwnedBy(String name, String description, Long ownerUserId) {
        this.checkNameRepeat(name, null);
        Long packageId = tenantPackageMapper.lambdaQuery()
            .eq(TenantPackageDO::getStatus, StatusEnum.ENABLE)
            .orderByAsc(TenantPackageDO::getSort)
            .orderByAsc(TenantPackageDO::getId)
            .last("LIMIT 1")
            .oneOpt()
            .map(TenantPackageDO::getId)
            .orElseThrow(() -> TenantException.of("ORG_PACKAGE_MISSING", "系统尚未配置可用套餐，无法创建组织"));
        packageService.checkStatus(packageId);

        Tenant entity = new Tenant();
        entity.setName(name);
        entity.setDescription(description);
        entity.setCode(this.generateCode());
        entity.setPackageId(packageId);
        entity.setStatus(StatusEnum.ENABLE);
        entity.setAdminUser(ownerUserId);
        TenantDO __tenantDo = AdminDoMapper.toDo(entity, TenantDO.class);
        baseMapper.insert(__tenantDo);
        AdminDoMapper.copyBack(__tenantDo, entity);

        TenantBO bo = new TenantBO();
        bo.setId(entity.getId());
        bo.setName(entity.getName());
        bo.setCode(entity.getCode());
        bo.setPackageId(packageId);
        bo.setAdminUserId(ownerUserId);
        tenantDataApis().forEach((key, value) -> value.init(bo));
        return entity.getId();
    }

    @Override
    public void bindAdminUser(Long tenantId, Long userId) {
        baseMapper.lambdaUpdate()
            .set(TenantDO::getAdminUser, userId)
            .eq(TenantDO::getId, tenantId)
            .update();
    }

    @Override
    public Tenant getEntity(Long id) {
        if (id == null) {
            return null;
        }
        return AdminDoMapper.toDomain(baseMapper.selectById(id), Tenant.class);
    }

    @Override
    public Tenant getEntityByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return AdminDoMapper.toDomain(baseMapper.lambdaQuery().eq(TenantDO::getCode, code.trim()).one(), Tenant.class);
    }

    @Override
    public List<Tenant> listEntitiesByIds(java.util.Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return AdminDoMapper.toDomainList(baseMapper.selectByIds(ids), Tenant.class);
    }

    public void beforeDelete(List<Long> ids) {
        // 在租户中执行数据清除
        for (Long id : ids) {
            TenantUtils.execute(id, () -> tenantDataApis().forEach((key, value) -> value.clear()));
        }
    }


    public void afterDelete(List<Long> ids) {
        RedisUtils.deleteByPattern(TENANT_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    @Cached(name = TENANT_KEY_PREFIX, key = "#domain")
    public Long getIdByDomain(String domain) {
        return baseMapper.lambdaQuery()
            .select(TenantDO::getId)
            .eq(TenantDO::getDomain, domain)
            .oneOpt()
            .map(TenantDO::getId)
            .orElse(null);
    }

    @Override
    @Cached(name = TENANT_KEY_PREFIX, key = "#code")
    public Long getIdByCode(String code) {
        return baseMapper.lambdaQuery()
            .select(TenantDO::getId)
            .eq(TenantDO::getCode, code)
            .oneOpt()
            .map(TenantDO::getId)
            .orElse(null);
    }

    @Override
    public void checkStatus(Long id) {
        // 默认租户
        if (tenantProperties.getDefaultTenantId().equals(id)) {
            return;
        }
        Tenant tenant = AdminDoMapper.toDomain(baseMapper.selectById(id), Tenant.class);
        if (tenant == null) {
            throw TenantException.notFound();
        }
        if (StatusEnum.DISABLE.equals(tenant.getStatus())) {
            throw TenantException.disabled();
        }
        if (tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now())) {
            throw TenantException.expired();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantMenu(List<Long> newMenuIds, Long packageId) {
        List<Long> tenantIdList = this.listIdByPackageId(packageId);
        if (CollUtil.isEmpty(tenantIdList)) {
            return;
        }
        // 所有租户角色：删除旧菜单
        tenantIdList.forEach(tenantId -> TenantUtils.execute(tenantId, () -> {
            // 删除旧菜单
            roleMenuApi.deleteByNotInMenuIds(newMenuIds);
            // 更新在线用户上下文
            Set<Long> roleIdSet = roleMenuApi.listRoleIdByNotInMenuIds(newMenuIds);
            roleIdSet.forEach(roleApi::updateUserContext);
        }));
        // 租户管理员：新增菜单
        tenantIdList.forEach(tenantId -> TenantUtils.execute(tenantId, () -> {
            Long roleId = roleApi.getIdByCode(RoleCodeEnum.TENANT_ADMIN.getCode());
            roleMenuApi.add(newMenuIds, roleId);
            // 更新在线用户上下文
            roleApi.updateUserContext(roleId);
        }));
        // 删除缓存
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }

    /**
     * 检查名称是否重复
     *
     * @param name 名称
     * @param id   ID
     */
    private void checkNameRepeat(String name, Long id) {
        if (baseMapper.lambdaQuery()
            .eq(TenantDO::getName, name)
            .ne(id != null, TenantDO::getId, id)
            .exists()) {
            throw TenantException.nameExists(name);
        }
    }

    /**
     * 检查域名是否重复
     *
     * @param domain 域名
     * @param id     ID
     */
    private void checkDomainRepeat(String domain, Long id) {
        if (baseMapper.lambdaQuery()
            .eq(TenantDO::getDomain, domain)
            .ne(id != null, TenantDO::getId, id)
            .exists()) {
            throw TenantException.domainExists(domain);
        }
    }


    /**
     * 生成租户编码
     *
     * @return 租户编码
     */
    private String generateCode() {
        String code;
        do {
            code = idGeneratorProvider.getRequired(CODE_GENERATOR_KEY).generateAsString();
        } while (baseMapper.lambdaQuery().eq(TenantDO::getCode, code).exists());
        return code;
    }

    /**
     * 根据套餐 ID 查询租户 ID 列表
     *
     * @param id 套餐 ID
     * @return 租户 ID 列表
     */
    private List<Long> listIdByPackageId(Long id) {
        return baseMapper.lambdaQuery()
            .select(TenantDO::getId)
            .eq(TenantDO::getPackageId, id)
            .list()
            .stream()
            .map(TenantDO::getId)
            .toList();
    }

    private Map<String, TenantDataApi> tenantDataApis() {
        return SpringUtil.getBeansOfType(TenantDataApi.class);
    }

    @Override
    public PageResult<TenantResp> findPage(TenantQuery query, PageQuery pageQuery) {
        long page = pageQuery.getPage() == null ? 1L : pageQuery.getPage();
        long pageSize = pageQuery.getPageSize() == null ? 10L : pageQuery.getPageSize();
        LambdaQueryWrapper<TenantDO> wrapper = QueryWrapperBuilder.build(query, TenantDO.class);
        wrapper.orderByDesc(TenantDO::getId);
        IPage<TenantDO> result = baseMapper.selectPage(new Page<>(page, pageSize), wrapper);
        Map<Long, String> packageNames = packageNames();
        return PageResult.of(result).map(item -> toResp(AdminDoMapper.toDomain(item, Tenant.class), packageNames));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TenantRequest req, Long id) {
        Tenant tenant = AdminDoMapper.toDomain(baseMapper.selectById(id), Tenant.class);
        if (tenant == null) {
            throw TenantException.notFound();
        }
        this.checkNameRepeat(req.getName(), id);
        this.checkDomainRepeat(req.getDomain(), id);
        Long previousPackageId = tenant.getPackageId();
        if (req.getPackageId() != null && !req.getPackageId().equals(previousPackageId)) {
            packageService.checkStatus(req.getPackageId());
        }
        tenant.setName(req.getName());
        tenant.setDomain(req.getDomain());
        tenant.setExpireTime(req.getExpireTime());
        tenant.setDescription(req.getDescription());
        if (req.getStatus() != null) {
            tenant.setStatus(req.getStatus());
        }
        if (req.getPackageId() != null) {
            tenant.setPackageId(req.getPackageId());
        }
        baseMapper.updateById(AdminDoMapper.toDo(tenant, TenantDO.class));
        if (req.getPackageId() != null && !req.getPackageId().equals(previousPackageId)) {
            syncAdminMenus(id, req.getPackageId());
        }
        RedisUtils.deleteByPattern(TENANT_KEY_PREFIX + StringConstants.ASTERISK);
    }

    @Override
    public List<LabelValueResult> dict(TenantQuery query) {
        List<LabelValueResult> options = new ArrayList<>();
        for (TenantResp item : list(query)) {
            options.add(new LabelValueResult<>(item.getName(), item.getId()));
        }
        return options;
    }

    @Override
    public TenantDetailResp get(Long id) {
        Tenant tenant = AdminDoMapper.toDomain(baseMapper.selectById(id), Tenant.class);
        if (tenant == null) {
            throw TenantException.notFound();
        }
        TenantResp base = toResp(tenant, packageNames());
        TenantDetailResp detail = new TenantDetailResp();
        detail.setId(base.getId());
        detail.setName(base.getName());
        detail.setCode(base.getCode());
        detail.setDomain(base.getDomain());
        detail.setExpireTime(base.getExpireTime());
        detail.setDescription(base.getDescription());
        detail.setStatus(base.getStatus());
        detail.setAdminUser(base.getAdminUser());
        detail.setAdminUsername(base.getAdminUsername());
        detail.setPackageId(base.getPackageId());
        detail.setPackageName(base.getPackageName());
        detail.setCreateTime(base.getCreateTime());
        detail.setUpdateTime(base.getUpdateTime());
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            if (!TenantLifecycleRules.deletable(id, tenantProperties.getDefaultTenantId())) {
                throw TenantException.deleteNotAllowed();
            }
            if (baseMapper.selectById(id) == null) {
                throw TenantException.notFound();
            }
        }
        beforeDelete(ids);
        ids.forEach(baseMapper::deleteById);
        afterDelete(ids);
    }

    @Override
    public List<TenantResp> list(TenantQuery query) {
        LambdaQueryWrapper<TenantDO> wrapper = QueryWrapperBuilder.build(query, TenantDO.class);
        wrapper.orderByDesc(TenantDO::getId);
        Map<Long, String> packageNames = packageNames();
        return AdminDoMapper.toDomainList(baseMapper.selectList(wrapper), Tenant.class).stream()
            .map(entity -> toResp(entity, packageNames))
            .toList();
    }

    @Override
    public List<Tree<Long>> tree(TenantQuery query, boolean b) {
        return List.of();
    }

    private Map<Long, String> packageNames() {
        return packageService.list(new PackageQuery()).stream()
            .filter(item -> item.getId() != null)
            .collect(Collectors.toMap(PackageResp::getId, PackageResp::getName, (left, right) -> left));
    }

    private TenantResp toResp(Tenant entity, Map<Long, String> packageNames) {
        TenantResp resp = new TenantResp();
        resp.setId(entity.getId());
        resp.setName(entity.getName());
        resp.setCode(entity.getCode());
        resp.setDomain(entity.getDomain());
        resp.setExpireTime(entity.getExpireTime());
        resp.setDescription(entity.getDescription());
        resp.setStatus(entity.getStatus());
        resp.setAdminUser(entity.getAdminUser());
        resp.setAdminUsername(entity.getAdminUsername());
        resp.setPackageId(entity.getPackageId());
        resp.setPackageName(packageNames.get(entity.getPackageId()));
        resp.setCreateUser(entity.getCreateUser());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateUser(entity.getUpdateUser());
        resp.setUpdateTime(entity.getUpdateTime());
        return resp;
    }

    private void syncAdminMenus(Long tenantId, Long packageId) {
        List<Long> menuIds = PackageMenuBinding.menusForTenantAdmin(packageMenuService.listMenuIdsByPackageId(packageId));
        if (menuIds.isEmpty()) {
            return;
        }
        TenantUtils.execute(tenantId, () -> {
            Long roleId = roleApi.getIdByCode(RoleCodeEnum.TENANT_ADMIN.getCode());
            if (roleId == null) {
                return;
            }
            roleMenuApi.deleteByNotInMenuIds(menuIds);
            roleMenuApi.add(menuIds, roleId);
            roleApi.updateUserContext(roleId);
        });
        RedisUtils.deleteByPattern(CacheConstants.ROLE_MENU_KEY_PREFIX + StringConstants.ASTERISK);
    }
}
