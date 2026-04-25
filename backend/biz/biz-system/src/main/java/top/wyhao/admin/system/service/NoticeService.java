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

import top.wyhao.admin.system.model.enums.NoticeMethods;
import top.wyhao.admin.system.model.entity.NoticeDO;
import top.wyhao.admin.system.model.query.NoticeQuery;
import top.wyhao.admin.system.model.bo.NoticeReq;
import top.wyhao.admin.system.model.vo.dashboard.DashboardNoticeResp;
import top.wyhao.admin.system.model.vo.notice.NoticeDetailResp;
import top.wyhao.admin.system.model.vo.notice.NoticeResp;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;
import top.wyhao.starter.web.core.model.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 公告业务接口
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
public interface NoticeService {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<NoticeResp> findPage(NoticeQuery query, PageQuery pageQuery);

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 列表信息
     */
    List<NoticeResp> list(NoticeQuery query, SortQuery sortQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    NoticeDetailResp get(Long id);

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return 自增 ID
     */
    Long create(@Valid NoticeReq req);

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    void update(@Valid NoticeReq req, Long id);

    /**
     * 删除
     *
     * @param ids ID 列表
     */
    void delete(@NotEmpty(message = "ID 不能为空") List<Long> ids);

    /**
     * 导出
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void export(NoticeQuery query, SortQuery sortQuery, HttpServletResponse response);

    /**
     * 查询字典列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 字典列表信息
     * @since 2.1.0
     */
    List<LabelValueResp> dict(NoticeQuery query, SortQuery sortQuery);

    /**
     * 发布公告
     *
     * @param notice 公告信息
     */
    void publish(NoticeDO notice);

    /**
     * 查询未读公告 ID 列表
     *
     * @param method 通知方式
     * @param userId 用户 ID
     * @return 未读公告 ID 响应参数
     */
    List<Long> listUnreadIdsByUserId(NoticeMethods method, Long userId);

    /**
     * 阅读公告
     *
     * @param id     公告 ID
     * @param userId 用户 ID
     */
    void readNotice(Long id, Long userId);

    /**
     * 查询仪表盘公告列表
     *
     * @return 仪表盘公告列表
     */
    List<DashboardNoticeResp> listDashboard();
}