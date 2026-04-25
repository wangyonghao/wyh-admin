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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.model.bo.DeptReq;
import top.wyhao.admin.system.model.entity.DeptDO;
import top.wyhao.admin.system.model.query.DeptQuery;
import top.wyhao.admin.system.model.vo.DeptResp;
import top.wyhao.admin.system.mapper.DeptMapper;
import top.wyhao.admin.system.service.DeptService;
import top.wyhao.admin.system.service.RoleDeptService;
import top.wyhao.admin.system.service.UserService;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.starter.core.util.TreeUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.data.enums.DatabaseType;
import top.wyhao.starter.data.util.DBMetaUtils;
import top.wyhao.starter.data.util.QueryWrapperUtil;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.starter.web.core.annotation.DictModel;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import javax.sql.DataSource;
import java.util.*;

/**
 * 部门业务实现
 *
 * @author Charles7c
 * @since 2023/1/22 17:55
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final RoleDeptService roleDeptService;
    private final DataSource dataSource;
    private final UserService userService;

    private final DeptMapper baseMapper;


    public void beforeCreate(DeptReq req) {
        this.checkNameRepeat(req.getName(), req.getParentId(), null);
        req.setAncestors(this.getAncestors(req.getParentId()));
    }

    public void beforeUpdate(DeptReq req, Long id) {
        this.checkNameRepeat(req.getName(), req.getParentId(), id);
        DeptDO oldDept = baseMapper.selectById(id);
        String oldName = oldDept.getName();
        StatusEnum newStatus = req.getStatus();
        Long oldParentId = oldDept.getParentId();
        if (Boolean.TRUE.equals(oldDept.getIsBuiltin())) {
            BizAssert.throwIfEqual(StatusEnum.DISABLE, newStatus, "[{}] 是系统内置部门，不允许禁用", oldName);
            BizAssert.throwIfNotEqual(req.getParentId(), oldParentId, "[{}] 是系统内置部门，不允许变更上级部门", oldName);
        }
        // 启用/禁用部门
        if (ObjectUtil.notEqual(newStatus, oldDept.getStatus())) {
            List<DeptDO> children = this.listChildren(id);
            long enabledChildrenCount = children.stream()
                .filter(d -> StatusEnum.ENABLE.equals(d.getStatus()))
                .count();
            BizAssert.isTrue(StatusEnum.DISABLE
                .equals(newStatus) && enabledChildrenCount > 0, "禁用 [{}] 前，请先禁用其所有下级部门", oldName);
            DeptDO oldParentDept = this.getByParentId(oldParentId);
            BizAssert.isTrue(StatusEnum.ENABLE.equals(newStatus) && StatusEnum.DISABLE
                .equals(oldParentDept.getStatus()), "启用 [{}] 前，请先启用其所有上级部门", oldName);
        }
        // 变更上级部门
        if (ObjectUtil.notEqual(req.getParentId(), oldParentId)) {
            // 更新祖级列表
            String newAncestors = this.getAncestors(req.getParentId());
            req.setAncestors(newAncestors);
            // 更新子级的祖级列表
            this.updateChildrenAncestors(newAncestors, oldDept.getAncestors(), id);
        }
    }

    public void beforeDelete(List<Long> ids) {
        List<DeptDO> list = baseMapper.lambdaQuery()
            .select(DeptDO::getName, DeptDO::getIsBuiltin)
            .in(DeptDO::getId, ids)
            .list();
        Optional<DeptDO> builtinData = list.stream().filter(DeptDO::getIsBuiltin).findFirst();
        BizAssert.isTrue(builtinData::isPresent, "所选部门 [{}] 是系统内置部门，不允许删除", builtinData.orElseGet(DeptDO::new)
            .getName());
        BizAssert.isTrue(this.countChildren(ids) > 0, "所选部门存在下级部门，不允许删除");
        BizAssert.isTrue(userService.countByDeptIds(ids) > 0, "所选部门存在用户关联，请解除关联后重试");
        // 删除角色和部门关联
        roleDeptService.deleteByDeptIds(ids);
    }

    @Override
    public PageResult<DeptResp> page(DeptQuery query, PageQuery pageQuery) {
        QueryWrapper<DeptDO> queryWrapper = QueryWrapperUtil.build(query);
        QueryWrapperUtil.applySort(queryWrapper, pageQuery.getSort(), DeptDO.class);
        IPage<DeptDO> page = baseMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()), queryWrapper);
        return PageResult.build(page, DeptResp.class);
    }

    @Override
    public List<DeptResp> list(DeptQuery query) {
        return this.list(query, DeptResp.class);
    }

    @Override
    public List<DeptResp> tree(DeptQuery query) {
        List<DeptResp> list = this.list(query, DeptResp.class);
        return TreeUtils.flatToTree(list,
                DeptResp::getId,
                DeptResp::getParentId,
                DeptResp::getChildren,
                DeptResp::setChildren);
    }

    @Override
    public DeptResp get(Long id) {
        DeptDO entity = baseMapper.selectById(id);
        return BeanUtil.toBean(entity, DeptResp.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeptReq req) {
        this.beforeCreate(req);
        DeptDO entity = BeanUtil.copyProperties(req, DeptDO.class);
        baseMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DeptReq req, Long id) {
        this.beforeUpdate(req, id);
        DeptDO entity = baseMapper.selectById(id);
        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        this.beforeDelete(ids);
        baseMapper.deleteByIds(ids);
    }

    @Override
    public void export(DeptQuery query, HttpServletResponse response) {
        List<DeptResp> list = this.list(query, DeptResp.class);
        ExcelUtils.export(list, "导出数据", DeptResp.class, response);
    }

    @Override
    public List<LabelValueResp> dict(DeptQuery query) {
        DictModel dictModel = DeptDO.class.getDeclaredAnnotation(DictModel.class);
        BizAssert.isNull(dictModel, "请添加并配置 @DictModel 字典结构信息");
        List<DeptResp> list = this.list(query);
        // 解析映射
        List<LabelValueResp> respList = new ArrayList<>(list.size());
        String labelKey = dictModel.labelKey().contains(StringConstants.DOT)
            ? CharSequenceUtil.subAfter(dictModel.labelKey(), StringConstants.DOT, true)
            : dictModel.labelKey();
        String valueKey = dictModel.valueKey().contains(StringConstants.DOT)
            ? CharSequenceUtil.subAfter(dictModel.valueKey(), StringConstants.DOT, true)
            : dictModel.valueKey();
        List<String> extraFieldNames = Arrays.stream(dictModel.extraKeys())
            .map(extraKey -> extraKey.contains(StringConstants.DOT)
                ? CharSequenceUtil.subAfter(extraKey, StringConstants.DOT, true)
                : extraKey)
            .map(CharSequenceUtil::toCamelCase)
            .toList();
        for (DeptResp entity : list) {
            LabelValueResp<Object> labelValueResp = new LabelValueResp<>();
            labelValueResp.setLabel(Convert.toStr(ReflectUtil.getFieldValue(entity, CharSequenceUtil
                .toCamelCase(labelKey))));
            labelValueResp.setValue(ReflectUtil.getFieldValue(entity, CharSequenceUtil.toCamelCase(valueKey)));
            respList.add(labelValueResp);
            if (CollUtil.isEmpty(extraFieldNames)) {
                continue;
            }
            // 额外数据
            Map<String, Object> extraMap = MapUtil.newHashMap(dictModel.extraKeys().length);
            for (String extraFieldName : extraFieldNames) {
                extraMap.put(extraFieldName, ReflectUtil.getFieldValue(entity, extraFieldName));
            }
            labelValueResp.setExtra(extraMap);
        }
        return respList;
    }

    /**
     * 查询列表
     *
     * @param query       查询条件
     * @param targetClass 指定类型
     * @return 列表信息
     */
    protected <E> List<E> list(DeptQuery query, Class<E> targetClass) {
        QueryWrapper<DeptDO> queryWrapper = QueryWrapperUtil.build(query);
        // 设置排序
        QueryWrapperUtil.applySort(queryWrapper, query.getSort(), DeptDO.class);
        List<DeptDO> entityList = baseMapper.selectList(queryWrapper);
        if (DeptDO.class == targetClass) {
            return (List<E>)entityList;
        }
        return BeanUtil.copyToList(entityList, targetClass);
    }

    @Override
    public List<DeptDO> listChildren(Long id) {
        DatabaseType databaseType = DBMetaUtils.getDatabaseTypeOrDefault(dataSource, DatabaseType.MYSQL);
        return baseMapper.lambdaQuery().apply(databaseType.findInSet(id, "ancestors")).list();
    }

    @Override
    public DeptDO getById(Long deptId) {
        return baseMapper.selectById(deptId);
    }

    /**
     * 检查名称是否重复
     *
     * @param name     名称
     * @param parentId 上级 ID
     * @param id       ID
     */
    private void checkNameRepeat(String name, Long parentId, Long id) {
        BizAssert.isTrue(baseMapper.lambdaQuery()
            .eq(DeptDO::getName, name)
            .eq(DeptDO::getParentId, parentId)
            .ne(id != null, DeptDO::getId, id)
            .exists(), "名称为 [{}] 的部门已存在", name);
    }

    /**
     * 获取祖级列表
     *
     * @param parentId 上级部门
     * @return 祖级列表
     */
    private String getAncestors(Long parentId) {
        DeptDO parentDept = this.getByParentId(parentId);
        return "%s,%s".formatted(parentDept.getAncestors(), parentId);
    }

    /**
     * 根据上级部门 ID 查询
     *
     * @param parentId 上级部门 ID
     * @return 上级部门信息
     */
    private DeptDO getByParentId(Long parentId) {
        DeptDO parentDept = baseMapper.selectById(parentId);
        BizAssert.isNull(parentDept, "上级部门不存在");
        return parentDept;
    }

    /**
     * 查询子部门数量
     *
     * @param ids ID 列表
     * @return 子部门数量
     */
    private Long countChildren(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0L;
        }
        DatabaseType databaseType = DBMetaUtils.getDatabaseTypeOrDefault(dataSource, DatabaseType.MYSQL);
        return ids.stream()
            .mapToLong(id -> baseMapper.lambdaQuery().apply(databaseType.findInSet(id, "ancestors")).count())
            .sum();
    }

    /**
     * 更新子部门祖级列表
     *
     * @param newAncestors 新祖级列表
     * @param oldAncestors 原祖级列表
     * @param id           ID
     */
    private void updateChildrenAncestors(String newAncestors, String oldAncestors, Long id) {
        List<DeptDO> children = this.listChildren(id);
        if (CollUtil.isEmpty(children)) {
            return;
        }
        List<DeptDO> list = new ArrayList<>(children.size());
        for (DeptDO child : children) {
            DeptDO dept = new DeptDO();
            dept.setId(child.getId());
            dept.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            list.add(dept);
        }
        baseMapper.updateById(list);
    }
}
