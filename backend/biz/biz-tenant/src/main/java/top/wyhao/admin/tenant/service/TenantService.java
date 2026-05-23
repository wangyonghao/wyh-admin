
package top.wyhao.admin.tenant.service;

import cn.hutool.core.lang.tree.Tree;
import jakarta.validation.Valid;
import top.wyhao.admin.tenant.model.query.TenantQuery;
import top.wyhao.admin.tenant.model.req.TenantReq;
import top.wyhao.admin.tenant.model.resp.TenantDetailResp;
import top.wyhao.admin.tenant.model.resp.TenantResp;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 租户业务接口
 *


 * @since 2024/11/26 17:20
 */
public interface TenantService {

    Long create(TenantReq req);

    /**
     * 根据域名查询
     *
     * @param domain 域名
     * @return ID
     */
    Long getIdByDomain(String domain);

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return ID
     */
    Long getIdByCode(String code);

    /**
     * 检查租户状态
     *
     * @param id ID
     */
    void checkStatus(Long id);

    /**
     * 更新租户菜单
     *
     * @param newMenuIds 新菜单 ID 列表
     * @param packageId  套餐 ID
     */
    void updateTenantMenu(List<Long> newMenuIds, Long packageId);

    PageResult<TenantResp> findPage(@Valid TenantQuery query, @Valid PageQuery pageQuery);

    void update(@Valid TenantReq req, Long id);

    List<LabelValueResult> dict(@Valid TenantQuery query, @Valid SortQuery sortQuery);

    TenantDetailResp get(Long id);

    void delete(List<Long> ids);

    List<TenantResp> list(@Valid TenantQuery query, @Valid SortQuery sortQuery);

    List<Tree<Long>> tree(@Valid TenantQuery query, @Valid SortQuery sortQuery, boolean b);
}