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

import top.wyhao.admin.system.model.entity.DictDO;
import top.wyhao.admin.system.model.query.DictQuery;
import top.wyhao.admin.system.model.vo.DictResult;
import top.wyhao.starter.data.service.IService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 字典业务接口
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
public interface DictService extends IService<DictDO> {

    PageResult<DictResult> page(DictQuery query, PageQuery pageQuery);

    /**
     * 根据字典类型查询字典列表
     *
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<LabelValueResp<String>> listByDictType(String dictType);

    /**
     * 查询枚举字典
     *
     * @return 枚举字典列表
     */
    List<LabelValueResp<String>> listEnumDict();
}