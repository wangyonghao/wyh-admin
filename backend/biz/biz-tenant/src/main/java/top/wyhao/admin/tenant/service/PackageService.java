
package top.wyhao.admin.tenant.service;

import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.admin.tenant.model.query.PackageQuery;
import top.wyhao.admin.tenant.model.req.PackageReq;
import top.wyhao.admin.tenant.model.resp.PackageDetailResp;
import top.wyhao.admin.tenant.model.resp.PackageResp;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.List;

/**
 * 套餐业务接口
 *

 * @since 2024/11/26 11:25
 */
public interface PackageService {

    /**
     * 检查套餐状态
     *
     * @param id ID
     */
    void checkStatus(Long id);

    PageResult<PackageResp> findPage(@Valid PackageQuery query, @Valid PageQuery pageQuery);

    List<PackageResp> list(@Valid PackageQuery query, @Valid SortQuery sortQuery);

    List<Tree<Long>> tree(@Valid PackageQuery query, @Valid SortQuery sortQuery, boolean b);

    void delete(List<Long> id);

    void export(@Valid PackageQuery query, @Valid SortQuery sortQuery, HttpServletResponse response);

    PackageDetailResp get(Long id);

    Long create(@Valid PackageReq req);

    void update(@Valid PackageReq req, Long id);
}