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

package top.wyhao.admin.system.mapper;

import com.alicp.jetcache.anno.Cached;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.admin.system.model.entity.DictDO;
import top.wyhao.starter.data.mapper.BaseMapper;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import java.util.List;

/**
 * 字典 Mapper
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Mapper
public interface DictMapper extends BaseMapper<DictDO> {

    /**
     * 根据字典类型查询字典列表
     *
     * @param dictType 字典类型
     * @return 字典列表
     */
    @Cached(key = "#dictType", name = CacheConstants.DICT_KEY_PREFIX)
    List<LabelValueResp<String>> listByDictType(@Param("dictType") String dictType);
}