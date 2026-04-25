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
import top.wyhao.admin.system.model.bo.SmsConfigReq;
import top.wyhao.admin.system.model.entity.SmsConfigDO;
import top.wyhao.admin.system.model.query.SmsConfigQuery;
import top.wyhao.admin.system.model.vo.SmsConfigResp;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import java.util.List;

/**
 * 短信配置业务接口
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
public interface SmsConfigService {

    /**
     * 设置默认配置
     *
     * @param id ID
     */
    void setDefaultConfig(Long id);

    /**
     * 获取默认短信配置
     *
     * @return 默认短信配置
     */
    SmsConfigDO getDefaultConfig();

    PageResult<SmsConfigResp> findPage(@Valid SmsConfigQuery query, @Valid PageQuery pageQuery);

    List<SmsConfigResp> list(@Valid SmsConfigQuery query, @Valid SortQuery sortQuery);

    List<Tree<Long>> tree(@Valid SmsConfigQuery query, @Valid SortQuery sortQuery, boolean b);

    Long create(@Valid SmsConfigReq req);

    SmsConfigResp get(Long id);

    void update(@Valid SmsConfigReq req, Long id);

    void delete(List<Long> id);

    void export(@Valid SmsConfigQuery query, @Valid SortQuery sortQuery, HttpServletResponse response);

    List<LabelValueResp> dict(@Valid SmsConfigQuery query, @Valid SortQuery sortQuery);
}