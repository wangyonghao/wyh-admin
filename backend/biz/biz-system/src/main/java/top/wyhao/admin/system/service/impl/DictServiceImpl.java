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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.mapper.DictMapper;
import top.wyhao.admin.system.model.entity.DictDO;
import top.wyhao.admin.system.model.query.DictQuery;
import top.wyhao.admin.system.model.vo.DictResult;
import top.wyhao.admin.system.service.DictService;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典业务实现
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<DictMapper, DictDO> implements DictService {
    @Override
    public PageResult<DictResult> page(DictQuery query, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<DictDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(query.getDictType()), DictDO::getDictType, query.getDictType())
            .like(StringUtils.isNotEmpty(query.getDescription()), DictDO::getDescription, query.getDescription())
            .eq(query.getEnabled(), DictDO::getEnabled, query.getEnabled());

        // 分页查询
        Page<DictDO> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        IPage<DictDO> dictPage = baseMapper.selectPage(page, queryWrapper);
        
        // 转换为响应对象
        IPage<DictResult> respPage = dictPage.convert(dict -> {
            DictResult resp = new DictResult();
            resp.setId(dict.getId());
            resp.setDictType(dict.getDictType());
            resp.setValue(dict.getValue());
            resp.setLabel(dict.getLabel());
            resp.setExtra(dict.getExtra());
            resp.setSort(dict.getSort());
            resp.setEnabled(dict.getEnabled());
            resp.setDescription(dict.getDescription());
            resp.setCreatedAt(dict.getCreatedAt());
            resp.setUpdatedAt(dict.getUpdatedAt());
            return resp;
        });
        
        return PageResult.build(respPage);
    }
    @Override
    public List<LabelValueResp<String>> listByDictType(String dictType) {
        return baseMapper.listByDictType(dictType);
    }

    @Override
    public List<LabelValueResp<String>> listEnumDict() {
        // 查询所有字典类型，去重
        List<String> dictTypes = this.lambdaQuery()
            .select(DictDO::getDictType)
            .groupBy(DictDO::getDictType)
            .list()
            .stream()
            .map(DictDO::getDictType)
            .collect(Collectors.toList());
        return CollUtils.mapToList(dictTypes, type -> new LabelValueResp<>(type, type));
    }
}