
package top.wyhao.system.admin.app.service.impl;

import top.wyhao.system.admin.infrastructure.persistence.AdminDoMapper;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageDO;

import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantDO;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.cmn.core.enums.StatusEnum;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.mybatis.query.QueryWrapperBuilder;
import top.wyhao.system.admin.domain.exception.PackageException;
import top.wyhao.system.admin.infrastructure.persistence.mapper.SysTenantMapper;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMapper;
import top.wyhao.system.admin.infrastructure.persistence.dataobject.TenantPackageMenuDraftDO;
import top.wyhao.system.admin.infrastructure.persistence.mapper.TenantPackageMenuDraftMapper;
import top.wyhao.system.admin.domain.model.Tenant;
import top.wyhao.system.admin.domain.model.TenantPackage;
import top.wyhao.system.admin.app.dto.PackageQuery;
import top.wyhao.system.admin.app.dto.PackageRequest;
import top.wyhao.system.admin.app.dto.PackageDetailResp;
import top.wyhao.system.admin.app.dto.PackageResp;
import top.wyhao.system.admin.app.service.PackageMenuService;
import top.wyhao.system.admin.app.service.PackageService;
import top.wyhao.web.core.model.PageQuery;

import java.util.List;

/**
 * 套餐业务实现
 *


 * @since 2024/11/26 11:25
 */
@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final TenantPackageMapper baseMapper;
    private final TenantPackageMenuDraftMapper draftMapper;
    private final PackageMenuService packageMenuService;
    private final SysTenantMapper tenantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PackageRequest req) {
        checkNameRepeat(req.getName(), null);
        TenantPackage entity = new TenantPackage();
        entity.setName(req.getName());
        entity.setSort(req.getSort() == null ? 999 : req.getSort());
        entity.setMenuCheckStrictly(req.getMenuCheckStrictly() == null || req.getMenuCheckStrictly());
        entity.setDescription(req.getDescription());
        entity.setStatus(StatusEnum.DISABLE);
        entity.setLifecycle("DRAFT");
        entity.setKind("MAIN");
        entity.setVersion(1);
        TenantPackageDO packageDo = AdminDoMapper.toDo(entity, TenantPackageDO.class);
        baseMapper.insert(packageDo);
        AdminDoMapper.copyBack(packageDo, entity);
        packageDo.setCode("p" + packageDo.getId());
        baseMapper.updateById(packageDo);
        replaceDraft(entity.getId(), req.getMenuIds());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PackageRequest req, Long id) {
        TenantPackage entity = require(id);
        if (entity.getPublishedAt() != null && req.getName() != null && !req.getName().equals(entity.getName())) {
            throw PackageException.nameLocked();
        }
        if ("DELISTED".equals(entity.getLifecycle())) {
            throw PackageException.of("PACKAGE_LIFECYCLE", "已下架套餐不能修改菜单");
        }
        checkNameRepeat(req.getName(), id);
        entity.setName(req.getName());
        if (req.getSort() != null) {
            entity.setSort(req.getSort());
        }
        if (req.getMenuCheckStrictly() != null) {
            entity.setMenuCheckStrictly(req.getMenuCheckStrictly());
        }
        entity.setDescription(req.getDescription());
        baseMapper.updateById(AdminDoMapper.toDo(entity, TenantPackageDO.class));
        replaceDraft(id, req.getMenuIds());
    }

    @Override
    public void checkStatus(Long id) {
        TenantPackage pkg = AdminDoMapper.toDomain(baseMapper.selectById(id), TenantPackage.class);
        if (pkg == null) {
            throw PackageException.notFound();
        }
        if (StatusEnum.DISABLE.equals(pkg.getStatus()) || !"PUBLISHED".equals(pkg.getLifecycle())) {
            throw PackageException.disabled();
        }
    }

    @Override
    public PageResult<PackageResp> findPage(PackageQuery query, PageQuery pageQuery) {
        long page = pageQuery.getPage() == null ? 1L : pageQuery.getPage();
        long pageSize = pageQuery.getPageSize() == null ? 10L : pageQuery.getPageSize();
        LambdaQueryWrapper<TenantPackageDO> wrapper = QueryWrapperBuilder.build(query, TenantPackageDO.class);
        wrapper.orderByAsc(TenantPackageDO::getSort).orderByDesc(TenantPackageDO::getId);
        IPage<TenantPackageDO> result = baseMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(result).map(item -> toResp(AdminDoMapper.toDomain(item, TenantPackage.class)));
    }

    @Override
    public List<PackageResp> list(PackageQuery query) {
        LambdaQueryWrapper<TenantPackageDO> wrapper = QueryWrapperBuilder.build(query, TenantPackageDO.class);
        wrapper.orderByAsc(TenantPackageDO::getSort).orderByDesc(TenantPackageDO::getId);
        return AdminDoMapper.toDomainList(baseMapper.selectList(wrapper), TenantPackage.class).stream().map(this::toResp).toList();
    }

    @Override
    public List<Tree<Long>> tree(PackageQuery query, boolean b) {
        return List.of();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            TenantPackage entity = require(id);
            if (entity.getPublishedAt() != null || (entity.getLifecycle() != null && !"DRAFT".equals(entity.getLifecycle()))) {
                throw PackageException.of("PACKAGE_LIFECYCLE", "只有未发布的套餐可以删除");
            }
            if (tenantMapper.lambdaQuery().eq(TenantDO::getPackageId, id).exists()) {
                throw PackageException.inUse();
            }
        }
        for (Long id : ids) {
            packageMenuService.add(List.of(), id);
            draftMapper.lambdaUpdate().eq(TenantPackageMenuDraftDO::getPackageId, id).remove();
            baseMapper.deleteById(id);
        }
    }

    @Override
    public void export(PackageQuery query, HttpServletResponse response) {

    }

    @Override
    public PackageDetailResp get(Long id) {
        TenantPackage entity = require(id);
        PackageDetailResp detail = new PackageDetailResp();
        copy(entity, detail);
        detail.setMenuIds(draftMenuIds(id));
        return detail;
    }

    private TenantPackage require(Long id) {
        TenantPackage entity = AdminDoMapper.toDomain(baseMapper.selectById(id), TenantPackage.class);
        if (entity == null) {
            throw PackageException.notFound();
        }
        return entity;
    }

    private void replaceDraft(Long packageId, List<Long> menuIds) {
        draftMapper.lambdaUpdate().eq(TenantPackageMenuDraftDO::getPackageId, packageId).remove();
        if (menuIds == null) {
            return;
        }
        for (Long menuId : menuIds) {
            if (menuId != null) {
                draftMapper.insert(new TenantPackageMenuDraftDO(packageId, menuId));
            }
        }
    }

    private List<Long> draftMenuIds(Long packageId) {
        return draftMapper.lambdaQuery()
            .eq(TenantPackageMenuDraftDO::getPackageId, packageId)
            .list()
            .stream()
            .map(TenantPackageMenuDraftDO::getMenuId)
            .toList();
    }

    private PackageResp toResp(TenantPackage entity) {
        PackageResp resp = new PackageResp();
        copy(entity, resp);
        return resp;
    }

    private void copy(TenantPackage entity, PackageResp resp) {
        resp.setId(entity.getId());
        resp.setName(entity.getName());
        resp.setSort(entity.getSort());
        resp.setMenuCheckStrictly(entity.getMenuCheckStrictly());
        resp.setDescription(entity.getDescription());
        resp.setStatus(entity.getStatus());
        resp.setCreateUser(entity.getCreateUser());
        resp.setCreateTime(entity.getCreateTime());
        resp.setUpdateUser(entity.getUpdateUser());
        resp.setUpdateTime(entity.getUpdateTime());
    }

    /**
     * 名称是否存在
     *
     * @param name 名称
     * @param id   ID
     */
    private void checkNameRepeat(String name, Long id) {
        if (baseMapper.lambdaQuery()
            .eq(TenantPackageDO::getName, name)
            .ne(id != null, TenantPackageDO::getId, id)
            .exists()) {
            throw PackageException.nameExists(name);
        }
    }

}