
package top.wyhao.system.admin.app.service;

import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.system.admin.app.dto.PackageQuery;
import top.wyhao.system.admin.app.dto.PackageRequest;
import top.wyhao.system.admin.app.dto.PackageDetailResp;
import top.wyhao.system.admin.app.dto.PackageResp;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;

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

    List<PackageResp> list(@Valid PackageQuery query);

    List<Tree<Long>> tree(@Valid PackageQuery query, boolean b);

    void delete(List<Long> id);

    void export(@Valid PackageQuery query, HttpServletResponse response);

    PackageDetailResp get(Long id);

    Long create(@Valid PackageRequest req);

    void update(@Valid PackageRequest req, Long id);
}