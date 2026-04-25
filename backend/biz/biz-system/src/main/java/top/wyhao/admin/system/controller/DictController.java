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

package top.wyhao.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.bo.DictRequest;
import top.wyhao.admin.system.model.entity.DictDO;
import top.wyhao.admin.system.model.query.DictQuery;
import top.wyhao.admin.system.model.vo.DictResult;
import top.wyhao.admin.system.service.DictService;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.core.model.R;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 字典管理 API
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Tag(name = "字典管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict")
public class DictController {

    private final DictService dictService;

    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("system:dict:page")
    @GetMapping("/page")
    public R<PageResult<DictResult>> page(DictQuery query, PageQuery pageQuery) {
        return R.ok(dictService.page(query, pageQuery));
    }

    @Operation(summary = "新增", description = "新增")
    @SaCheckPermission("system:dict:create")
    @PostMapping
    public R<Void> create(@Valid @RequestBody DictRequest req) {
        // 检查字典类型+值是否重复
        BizAssert.isTrue(dictService.lambdaQuery()
            .eq(DictDO::getDictType, req.getDictType())
            .eq(DictDO::getValue, req.getValue())
            .exists(), "字典类型 [{}] 中值为 [{}] 的字典已存在", req.getDictType(), req.getValue());
        
        DictDO dict = new DictDO();
        dict.setDictType(req.getDictType());
        dict.setValue(req.getValue());
        dict.setLabel(req.getLabel());
        dict.setExtra(req.getExtra());
        dict.setSort(req.getSort() != null ? req.getSort() : 0);
        dict.setEnabled(req.getEnabled() != null ? req.getEnabled() : true);
        dict.setDescription(req.getDescription());
        dictService.save(dict);
        return R.ok();
    }

    @Operation(summary = "修改", description = "修改")
    @SaCheckPermission("system:dict:update")
    @PutMapping("/{id}")
    public R<Void> update(@Valid @RequestBody DictRequest req, @Parameter(description = "ID", example = "1") @PathVariable Long id) {
        // 检查字典类型+值是否重复
        BizAssert.isTrue(dictService.lambdaQuery()
            .eq(DictDO::getDictType, req.getDictType())
            .eq(DictDO::getValue, req.getValue())
            .ne(DictDO::getId, id)
            .exists(), "字典类型 [{}] 中值为 [{}] 的字典已存在", req.getDictType(), req.getValue());
        
        DictDO dict = new DictDO();
        dict.setId(id);
        dict.setDictType(req.getDictType());
        dict.setValue(req.getValue());
        dict.setLabel(req.getLabel());
        dict.setExtra(req.getExtra());
        dict.setSort(req.getSort());
        dict.setEnabled(req.getEnabled());
        dict.setDescription(req.getDescription());
        dictService.updateById(dict);
        
        // 清除缓存
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + req.getDictType());
        return R.ok();
    }

    @Operation(summary = "批量删除", description = "批量删除")
    @SaCheckPermission("system:dict:delete")
    @DeleteMapping
    public R<Void> delete(@RequestBody List<Long> ids) {
        BizAssert.isTrue(ids.isEmpty(), "请选择要删除的数据");
        
        // 获取需要清除缓存的字典类型
        List<String> dictTypes = dictService.lambdaQuery()
            .select(DictDO::getDictType)
            .in(DictDO::getId, ids)
            .groupBy(DictDO::getDictType)
            .list()
            .stream()
            .map(DictDO::getDictType)
            .toList();
        
        dictService.removeByIds(ids);
        
        // 清除缓存
        dictTypes.forEach(type -> RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + type));
        return R.ok();
    }

    @Operation(summary = "清除缓存", description = "清除缓存")
    @SaCheckPermission("system:dict:clearCache")
    @DeleteMapping("/cache/{dictType}")
    public R<Void> clearCache(@Parameter(description = "字典类型", example = "notice_type") @PathVariable String dictType) {
        RedisUtils.deleteByPattern(CacheConstants.DICT_KEY_PREFIX + dictType);
        return R.ok();
    }
}