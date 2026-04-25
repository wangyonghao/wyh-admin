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
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import top.wyhao.admin.system.model.query.LogQuery;
import top.wyhao.admin.system.model.vo.log.LogDetailResp;
import top.wyhao.admin.system.model.vo.log.LogResp;
import top.wyhao.starter.core.model.LoginUser;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.log.OperationLog;

/**
 * 操作日志 Service
 */
public interface OperationLogService {

    @Async
    @EventListener
    void log(OperationLog operationLog);

    @Async
    void recordLoginLog(LoginUser loginUser);

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<LogResp> page(LogQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    LogDetailResp get(Long id);

    /**
     * 导出登录日志
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void exportLoginLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response);

    /**
     * 导出操作日志
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void exportOperationLog(LogQuery query, SortQuery sortQuery, HttpServletResponse response);
}
