/*
 * Copyright (c) 2022-present wangyonghao Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.wyhao.admin.system.service;

import jakarta.servlet.http.HttpServletResponse;
import top.wyhao.admin.system.model.bo.DeptReq;
import top.wyhao.admin.system.model.entity.DeptDO;
import top.wyhao.admin.system.model.query.DeptQuery;
import top.wyhao.admin.system.model.vo.DeptResp;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

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
     * @param sortQuery 排序查询条件
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
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void export(DeptQuery query, HttpServletResponse response);

    /**
     * 查询字典列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 字典列表信息
     */
    List<LabelValueResp> dict(DeptQuery query);

    /**
     * 查询子部门列表
     *
     * @param id ID
     * @return 子部门列表
     */
    List<DeptDO> listChildren(Long id);

    DeptDO getById(Long deptId);
}
