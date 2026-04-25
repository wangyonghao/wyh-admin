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

package top.wyhao.admin.system.service.impl;

import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.model.query.SmsLogQuery;
import top.wyhao.admin.system.model.bo.SmsLogReq;
import top.wyhao.admin.system.model.vo.SmsLogResult;
import top.wyhao.admin.system.service.SmsLogService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import java.util.List;

/**
 * 短信日志业务实现
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Service
public class SmsLogServiceImpl implements SmsLogService {
    @Override
    public Long create(SmsLogReq req) {
        return 0L;
    }

    @Override
    public List<Tree<Long>> tree(SmsLogQuery query, SortQuery sortQuery, boolean b) {
        return List.of();
    }

    @Override
    public void update(SmsLogReq req, Long id) {

    }

    @Override
    public void delete(List<Long> id) {

    }

    @Override
    public List<LabelValueResp> dict(SmsLogQuery query, SortQuery sortQuery) {
        return List.of();
    }

    @Override
    public void export(SmsLogQuery query, SortQuery sortQuery, HttpServletResponse response) {

    }

    @Override
    public SmsLogResult get(Long id) {
        return null;
    }

    @Override
    public PageResult<SmsLogResult> findPage(SmsLogQuery query, PageQuery pageQuery) {
        return null;
    }

    @Override
    public List<SmsLogResult> list(SmsLogQuery query, SortQuery sortQuery) {
        return List.of();
    }
}