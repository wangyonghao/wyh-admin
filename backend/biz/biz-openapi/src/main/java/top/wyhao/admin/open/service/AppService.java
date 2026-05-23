
package top.wyhao.admin.open.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.admin.open.model.entity.SysApp;
import top.wyhao.admin.open.model.query.AppQuery;
import top.wyhao.admin.open.model.req.AppReq;
import top.wyhao.admin.open.model.resp.AppResp;
import top.wyhao.admin.open.model.resp.AppSecretResp;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 应用业务接口


 * @since 2024/10/17 16:03
 */
public interface AppService {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<AppResp> findPage(@Valid AppQuery query, @Valid PageQuery pageQuery);


    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 列表信息
     */
    List<AppResp> list(@Valid AppQuery query, @Valid SortQuery sortQuery);

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return 自增 ID
     */
    Long create(@Valid AppReq req);
    /**
     * 获取密钥
     *
     * @param id ID
     * @return 密钥信息
     */
    AppSecretResp getSecret(Long id);

    /**
     * 重置密钥
     *
     * @param id ID
     */
    void resetSecret(Long id);

    /**
     * 根据 Access Key 查询
     *
     * @param accessKey Access Key
     * @return 应用信息
     */
    SysApp getByAccessKey(String accessKey);

    void export(AppQuery query, SortQuery sortQuery, HttpServletResponse response);

    void delete(List<Long> ids);

    void update(AppReq req, Long id);
}