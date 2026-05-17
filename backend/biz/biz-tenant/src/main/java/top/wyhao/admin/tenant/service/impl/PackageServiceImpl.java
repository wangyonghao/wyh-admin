
package top.wyhao.admin.tenant.service.impl;

import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wyhao.admin.tenant.mapper.PackageMapper;
import top.wyhao.admin.tenant.mapper.TenantMapper;
import top.wyhao.admin.tenant.model.entity.TenantPackage;
import top.wyhao.admin.tenant.model.query.PackageQuery;
import top.wyhao.admin.tenant.model.req.PackageReq;
import top.wyhao.admin.tenant.model.resp.PackageDetailResp;
import top.wyhao.admin.tenant.model.resp.PackageResp;
import top.wyhao.admin.tenant.service.PackageService;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.List;

/**
 * 套餐业务实现
 *
 * @author 小熊
 * @author Charles7c
 * @since 2024/11/26 11:25
 */
@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final PackageMapper baseMapper;
    private final TenantMapper tenantMapper;

    @Override
    public Long create(PackageReq req) {
        return 0L;
    }

    @Override
    public void update(PackageReq req, Long id) {

    }


    public void beforeDelete(List<Long> ids) {
        BizAssert.isTrue(tenantMapper.countByPackageIds(ids) > 0, "所选套餐存在关联租户，不允许删除");
    }

    @Override
    public void checkStatus(Long id) {

    }

    @Override
    public PageResult<PackageResp> findPage(PackageQuery query, PageQuery pageQuery) {
        return null;
    }

    @Override
    public List<PackageResp> list(PackageQuery query, SortQuery sortQuery) {
        return List.of();
    }

    @Override
    public List<Tree<Long>> tree(PackageQuery query, SortQuery sortQuery, boolean b) {
        return List.of();
    }

    @Override
    public void delete(List<Long> id) {

    }

    @Override
    public void export(PackageQuery query, SortQuery sortQuery, HttpServletResponse response) {

    }

    @Override
    public PackageDetailResp get(Long id) {
        return null;
    }

    /**
     * 名称是否存在
     *
     * @param name 名称
     * @param id   ID
     */
    private void checkNameRepeat(String name, Long id) {
        BizAssert.isTrue(baseMapper.lambdaQuery()
            .eq(TenantPackage::getName, name)
            .ne(id != null, TenantPackage::getId, id)
            .exists(), "名称为 [{}] 的套餐已存在", name);
    }

}