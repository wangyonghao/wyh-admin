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
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import cn.idev.excel.FastExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.admin.system.mapper.ConfigMapper;
import top.wyhao.admin.system.model.bo.ConfigRequest;
import top.wyhao.admin.system.model.entity.ConfigDO;
import top.wyhao.admin.system.model.query.ConfigQuery;
import top.wyhao.admin.system.model.vo.ConfigResult;
import top.wyhao.admin.system.model.vo.config.*;
import top.wyhao.admin.system.service.ConfigService;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.data.util.QueryWrapperUtil;
import top.wyhao.starter.excel.util.ExcelUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置业务实现
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigMapper configMapper;

    @Override
    public PageResult<ConfigResult> page(ConfigQuery query, PageQuery pageQuery) {
        QueryWrapper<ConfigDO> queryWrapper = this.buildQueryWrapper(query);
        QueryWrapperUtil.applySort(queryWrapper, pageQuery.getSort(), ConfigDO.class);
        IPage<ConfigResult> page = configMapper.selectConfigPage(
                new Page<>(pageQuery.getPage(), pageQuery.getSize()),
                queryWrapper
        );
        return PageResult.build(page);
    }

    @Override
    public ConfigResult detail(Long id) {
        ConfigDO configDO = configMapper.selectById(id);
        BizAssert.notNull(configDO, "配置不存在");

        return BeanUtil.copyProperties(configDO, ConfigResult.class);
    }

    @Override
    public ConfigResult getByKey(String configKey) {
        QueryWrapper<ConfigDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", configKey);
        ConfigDO configDO = configMapper.selectOne(queryWrapper);
        BizAssert.notNull(configDO, "配置不存在");

        return BeanUtil.copyProperties(configDO, ConfigResult.class);
    }

    @Override
    public SiteConfigVO getSiteConfig() {
        return this.getConfig("site", SiteConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSiteConfig(SiteConfigVO config) {
        this.updateConfig("site", config);
    }

    @Override
    public LoginConfigVO getLoginConfig() {
        return this.getConfig("login", LoginConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginConfig(LoginConfigVO config) {
        this.updateConfig("login", config);
    }

    @Override
    public RegisterConfigVO getRegisterConfig() {
        return this.getConfig("register", RegisterConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRegisterConfig(RegisterConfigVO config) {
        this.updateConfig("register", config);
    }

    @Override
    public EmailConfigVO getEmailConfig() {
        return this.getConfig("email", EmailConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmailConfig(EmailConfigVO config) {
        this.updateConfig("email", config);
    }

    @Override
    public SmsConfigVO getSmsConfig() {
        return this.getConfig("sms", SmsConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSmsConfig(SmsConfigVO config) {
        this.updateConfig("sms", config);
    }

    @Override
    public StorageConfigVO getStorageConfig() {
        return this.getConfig("storage", StorageConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStorageConfig(StorageConfigVO config) {
        this.updateConfig("storage", config);
    }

    @Override
    public SecurityConfigVO getSecurityConfig() {
        return this.getConfig("security", SecurityConfigVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSecurityConfig(SecurityConfigVO config) {
        this.updateConfig("security", config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ConfigRequest request) {
        // 检查唯一性
        this.checkUnique(request.getConfigKey(), null);

        ConfigDO configDO = BeanUtil.copyProperties(request, ConfigDO.class);
        configMapper.insert(configDO);
        return configDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ConfigRequest request) {
        ConfigDO oldConfig = configMapper.selectById(id);
        BizAssert.notNull(oldConfig, "配置不存在");

        // 检查唯一性（排除自己）
        if (CharSequenceUtil.isNotBlank(request.getConfigKey())) {
            this.checkUnique(request.getConfigKey(), id);
        }

        ConfigDO configDO = BeanUtil.copyProperties(request, ConfigDO.class);
        configDO.setId(id);

        int updated = configMapper.updateById(configDO);
        BizAssert.isTrue(updated > 0, "更新失败，配置可能已被修改，请刷新后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByKey(String configKey, ConfigRequest request) {
        QueryWrapper<ConfigDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", configKey);
        ConfigDO existConfig = configMapper.selectOne(queryWrapper);
        BizAssert.notNull(existConfig, "配置不存在");

        ConfigDO configDO = new ConfigDO();
        configDO.setId(existConfig.getId());
        configDO.setConfigValue(request.getConfigValue());
        configDO.setDescription(request.getDescription());
        configDO.setVersion(request.getVersion());

        int updated = configMapper.updateById(configDO);
        BizAssert.isTrue(updated > 0, "更新失败，配置可能已被修改，请刷新后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        configMapper.deleteByIds(ids);
    }

    @Override
    public void export(ConfigQuery query, SortQuery sortQuery, HttpServletResponse response) {
        QueryWrapper<ConfigDO> queryWrapper = this.buildQueryWrapper(query);
        QueryWrapperUtil.applySort(queryWrapper, sortQuery.getSort(), ConfigDO.class);
        List<ConfigDO> list = configMapper.selectList(queryWrapper);

        List<ConfigResult> resultList = list.stream()
                .map(config -> BeanUtil.copyProperties(config, ConfigResult.class))
                .collect(Collectors.toList());

        ExcelUtils.export(resultList, "系统配置", ConfigResult.class, response);
    }

    /**
     * 获取配置并转换为指定类型
     *
     * @param configKey 配置键
     * @param clazz     目标类型
     * @param <T>       泛型类型
     * @return 配置对象
     */
    private <T> T getConfig(String configKey, Class<T> clazz) {
        QueryWrapper<ConfigDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", configKey);
        ConfigDO configDO = configMapper.selectOne(queryWrapper);

        if (configDO == null || CharSequenceUtil.isBlank(configDO.getConfigValue())) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("创建配置对象失败", e);
                throw new BusinessException("获取配置失败");
            }
        }

        try {
            return JSONUtil.toBean(configDO.getConfigValue(), clazz);
        } catch (Exception e) {
            log.error("解析配置失败: {}", configKey, e);
            throw new BusinessException("配置格式错误");
        }
    }

    /**
     * 更新配置
     *
     * @param configKey 配置键
     * @param config    配置对象
     */
    private void updateConfig(String configKey, Object config) {
        ConfigDO existConfig = configMapper.lambdaQuery().eq(ConfigDO::getConfigKey, configKey).one();
        String configValue = JSONUtil.toJsonStr(config);

        if (existConfig != null) {
            // 更新现有配置
            ConfigDO updateConfig = new ConfigDO();
            updateConfig.setId(existConfig.getId());
            updateConfig.setConfigValue(configValue);

            int updated = configMapper.updateById(updateConfig);
            BizAssert.isTrue(updated > 0, "更新配置失败");
        } else {
            // 创建新配置
            ConfigDO newConfig = new ConfigDO();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            newConfig.setDescription(configKey + "配置");

            configMapper.insert(newConfig);
        }
    }

    /**
     * 构建查询条件
     *
     * @param query 查询条件
     * @return 查询包装器
     */
    private QueryWrapper<ConfigDO> buildQueryWrapper(ConfigQuery query) {
        String configKey = query.getConfigKey();
        String searchWords = query.getSearchWords();

        return new QueryWrapper<ConfigDO>()
                .like(CharSequenceUtil.isNotBlank(configKey), "config_key", configKey)
                .and(CharSequenceUtil.isNotBlank(searchWords), q -> q
                        .like("config_key", searchWords)
                        .or()
                        .like("description", searchWords))
                .orderByAsc("config_key");
    }

    /**
     * 检查唯一性
     *
     * @param configKey 配置键
     * @param id        当前配置ID（更新时传入，新增时传null）
     */
    private void checkUnique(String configKey, Long id) {
        QueryWrapper<ConfigDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", configKey);
        if (id != null) {
            queryWrapper.ne("id", id);
        }

        Long count = configMapper.selectCount(queryWrapper);
        BizAssert.isTrue(count == 0, "配置键已存在");
    }
}