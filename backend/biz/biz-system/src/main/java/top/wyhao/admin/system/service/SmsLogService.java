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

import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import top.wyhao.admin.system.model.query.SmsLogQuery;
import top.wyhao.admin.system.model.bo.SmsLogReq;
import top.wyhao.admin.system.model.vo.SmsLogResult;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import java.util.List;

/**
 * 短信日志业务接口
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
public interface SmsLogService {
    Long create(SmsLogReq req);

    List<Tree<Long>> tree(@Valid SmsLogQuery query, @Valid SortQuery sortQuery, boolean b);

    void update(@Valid SmsLogReq req, Long id);

    void delete(List<Long> id);

    List<LabelValueResp> dict(@Valid SmsLogQuery query, @Valid SortQuery sortQuery);

    void export(@Valid SmsLogQuery query, @Valid SortQuery sortQuery, HttpServletResponse response);

    SmsLogResult get(Long id);

    PageResult<SmsLogResult> findPage(@Valid SmsLogQuery query, @Valid PageQuery pageQuery);

    List<SmsLogResult> list(@Valid SmsLogQuery query, @Valid SortQuery sortQuery);
}