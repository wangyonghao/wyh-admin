
package top.wyhao.admin.system.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wyhao.admin.system.model.bo.DeptReq;
import top.wyhao.admin.system.entity.SysDept;
import top.wyhao.admin.system.model.query.DeptQuery;
import top.wyhao.admin.system.model.vo.DeptResp;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 部门业务接口
 */
public interface DeptService{

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    PageResult<DeptResp> page(DeptQuery query, PageQuery pageQuery);

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @return 列表信息
     */
    List<DeptResp> list(DeptQuery query);

    /**
     * 查询部门树
     *
     * @param query     查询条件
     * @return 树列表信息
     */
    List<DeptResp> tree(DeptQuery query);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    DeptResp get(Long id);

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return 自增 ID
     */
    Long create(DeptReq req);

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(DeptReq req, Long id);

    /**
     * 删除
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 导出
     *
     * @param query     查询条件
     * @param response  响应对象
     */
    void export(DeptQuery query, HttpServletResponse response);

    /**
     * 查询子部门列表
     *
     * @param id ID
     * @return 子部门列表
     */
    List<SysDept> listChildren(Long id);

    SysDept getById(Long deptId);
}
