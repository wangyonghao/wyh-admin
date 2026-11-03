
package top.wyhao.system.admin.app.service;

import cn.hutool.core.lang.tree.Tree;
import jakarta.validation.Valid;
import top.wyhao.system.admin.domain.model.Tenant;
import top.wyhao.system.admin.app.dto.TenantQuery;
import top.wyhao.system.admin.app.dto.TenantRequest;
import top.wyhao.system.admin.app.dto.TenantDetailResp;
import top.wyhao.system.admin.app.dto.TenantResp;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 租户业务接口
 *


 * @since 2024/11/26 17:20
 */
public interface TenantService {

    Long create(TenantRequest req);

    /**
     * 用户自助创建组织并以自己为管理员。
     */
    Long createOwnedBy(String name, String description, Long ownerUserId);

    /**
     * 绑定租户管理员用户。
     */
    void bindAdminUser(Long tenantId, Long userId);

    /**
     * 按 ID 查询实体（跨模块用）。
     */
    Tenant getEntity(Long id);

    /**
     * 按编码查询实体。
     */
    Tenant getEntityByCode(String code);

    java.util.List<Tenant> listEntitiesByIds(java.util.Collection<Long> ids);

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

    void update(@Valid TenantRequest req, Long id);

    List<LabelValueResult> dict(@Valid TenantQuery query);

    TenantDetailResp get(Long id);

    void delete(List<Long> ids);

    List<TenantResp> list(@Valid TenantQuery query);

    List<Tree<Long>> tree(@Valid TenantQuery query, boolean b);
}