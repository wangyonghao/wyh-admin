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
import cn.hutool.core.lang.tree.Tree;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.model.query.SmsConfigQuery;
import top.wyhao.admin.system.mapper.SmsConfigMapper;
import top.wyhao.starter.core.enums.StatusEnum;
import top.wyhao.admin.system.config.sms.SmsConfigUtil;
import top.wyhao.admin.system.model.entity.SmsConfigDO;
import top.wyhao.admin.system.model.bo.SmsConfigReq;
import top.wyhao.admin.system.model.vo.SmsConfigResp;
import top.wyhao.admin.system.service.SmsConfigService;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import java.util.List;

/**
 * 短信配置业务实现
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Service
@RequiredArgsConstructor
public class SmsConfigServiceImpl implements SmsConfigService {
    private final SmsConfigMapper baseMapper;

    public void afterCreate(SmsConfigReq req, SmsConfigDO entity) {
        this.load(entity);
    }

    public void afterUpdate(SmsConfigReq req, SmsConfigDO entity) {
        // 重新加载配置
        // 先卸载
        this.unload(entity.getId().toString());
        // 再加载
        this.load(entity);
    }

    public void afterDelete(List<Long> ids) {
        for (Long id : ids) {
            this.unload(id.toString());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultConfig(Long id) {
        SmsConfigDO smsConfig = baseMapper.selectById(id);
        if (Boolean.TRUE.equals(smsConfig.getIsDefault())) {
            return;
        }
        // 启用状态才能设为默认配置
        BizAssert.throwIfEqual(StatusEnum.DISABLE, smsConfig.getStatus(), "请先启用所选配置");
        baseMapper.lambdaUpdate().eq(SmsConfigDO::getIsDefault, true).set(SmsConfigDO::getIsDefault, false).update();
        baseMapper.lambdaUpdate().eq(SmsConfigDO::getId, id).set(SmsConfigDO::getIsDefault, true).update();
    }

    @Override
    public SmsConfigDO getDefaultConfig() {
        return baseMapper.lambdaQuery()
            .eq(SmsConfigDO::getIsDefault, true)
            .eq(SmsConfigDO::getStatus, StatusEnum.ENABLE)
            .one();
    }

    @Override
    public PageResult<SmsConfigResp> findPage(SmsConfigQuery query, PageQuery pageQuery) {
        return null;
    }

    @Override
    public List<SmsConfigResp> list(SmsConfigQuery query, SortQuery sortQuery) {
        return List.of();
    }

    @Override
    public List<Tree<Long>> tree(SmsConfigQuery query, SortQuery sortQuery, boolean b) {
        return List.of();
    }

    @Override
    public Long create(SmsConfigReq req) {
        return 0L;
    }

    @Override
    public SmsConfigResp get(Long id) {
        return null;
    }

    @Override
    public void update(SmsConfigReq req, Long id) {

    }

    @Override
    public void delete(List<Long> id) {

    }

    @Override
    public void export(SmsConfigQuery query, SortQuery sortQuery, HttpServletResponse response) {

    }

    @Override
    public List<LabelValueResp> dict(SmsConfigQuery query, SortQuery sortQuery) {
        return List.of();
    }

    /**
     * 加载配置
     *
     * @param entity 配置信息
     */
    private void load(SmsConfigDO entity) {
        SmsConfigDO smsConfigDO = baseMapper.selectById(entity.getId());
        SmsConfigResp smsConfig = BeanUtil.copyProperties(smsConfigDO, SmsConfigResp.class);
        BaseConfig config = SmsConfigUtil.from(smsConfig);
        if (config != null) {
            SmsFactory.createSmsBlend(config);
        }
    }

    /**
     * 卸载配置
     *
     * @param configId 配置 ID
     */
    private void unload(String configId) {
        if (SmsFactory.getSmsBlend(configId) != null) {
            SmsFactory.unregister(configId);
        }
    }
}