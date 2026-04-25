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
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONException;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.model.bo.SettingsRequest;
import top.wyhao.admin.system.model.bo.SettingsResetRequest;
import top.wyhao.admin.system.model.entity.SettingsDO;
import top.wyhao.admin.system.model.query.SettingsQuery;
import top.wyhao.admin.system.model.vo.SettingsResult;
import top.wyhao.admin.system.model.enums.ConfigCategory;
import top.wyhao.admin.system.model.enums.PasswordPolicies;
import top.wyhao.admin.system.mapper.SettingsMapper;
import top.wyhao.admin.system.service.SettingsService;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.core.constant.StringConstants;
import top.wyhao.starter.core.util.CollUtils;
import top.wyhao.starter.core.util.validation.ValidationUtils;
import top.wyhao.starter.data.util.QueryWrapperUtil;
import top.wyhao.starter.mail.MailAccount;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参数业务实现
 *
 * @author Bull-BCLS
 * @since 2023/8/26 19:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {
    private static final String CACHE_KEY_PREFIX = "sys_config";

    private final SettingsMapper settingsMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<SettingsResult> list(SettingsQuery query) {
        return BeanUtil.copyToList(settingsMapper.selectList(QueryWrapperUtil.build(query)), SettingsResult.class);
    }

    @Override
    public void updateJsonValue(String key, Object value) {
        String json;
        try {
            json = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JSONException(e);
        }
        this.updateValue(key, json);
    }

    @Override
    public void updateValue(String key, String value) {
        settingsMapper.lambdaUpdate().set(SettingsDO::getValue, value)
                .eq(SettingsDO::getCode, key);
    }

    @Override
    @Cached(key = "#category", name = CacheConstants.CONFIG_KEY_PREFIX + "MAP:")
    public Map<String, String> getByCategory(ConfigCategory category) {
        return settingsMapper.selectByCategory(category.name())
                .stream()
                .collect(Collectors.toMap(SettingsDO::getCode, o -> CharSequenceUtil.emptyIfNull(ObjectUtil.defaultIfNull(o
                        .getValue(), o.getDefaultValue())), (oldVal, newVal) -> oldVal));
    }

    @Override
    public void add(SettingsRequest config) {

    }

    @Override
    public void update(List<SettingsRequest> configs) {
        // 非空校验
        List<Long> idList = CollUtils.mapToList(configs, SettingsRequest::getId);
        List<SettingsDO> configList = settingsMapper.selectByIds(idList);
        Map<String, SettingsDO> configMap = configList.stream()
                .collect(Collectors.toMap(SettingsDO::getCode, Function.identity(), (existing, replacement) -> existing));
        for (SettingsRequest req : configs) {
            SettingsDO config = configMap.get(req.getCode());
            ValidationUtils.throwIfNull(config, "参数 [{}] 不存在", req.getCode());
            if (CharSequenceUtil.isNotBlank(config.getDefaultValue())) {
                ValidationUtils.throwIfBlank(req.getValue(), "参数 [{}] 的值不能为空", config.getName());
            }
        }
        // 校验密码策略参数取值范围
        Map<String, String> passwordPolicyConfigMap = configs.stream()
                .filter(config -> CharSequenceUtil.startWith(config.getCode(), PasswordPolicies.CATEGORY
                        .name() + StringConstants.UNDERLINE))
                .collect(Collectors.toMap(SettingsRequest::getCode, SettingsRequest::getValue, (oldVal, newVal) -> oldVal));
        for (Map.Entry<String, String> passwordPolicyConfigEntry : passwordPolicyConfigMap.entrySet()) {
            String code = passwordPolicyConfigEntry.getKey();
            String value = passwordPolicyConfigEntry.getValue();
            ValidationUtils.throwIf(!NumberUtil.isNumber(value), "参数 [%s] 的值必须为数字", code);
            PasswordPolicies passwordPolicy = PasswordPolicies.valueOf(code);
            passwordPolicy.validateRange(Integer.parseInt(value), passwordPolicyConfigMap);
        }
        RedisUtils.deleteByPattern(CacheConstants.CONFIG_KEY_PREFIX + StringConstants.ASTERISK);
        settingsMapper.updateById(BeanUtil.copyToList(configs, SettingsDO.class));
    }

    @Override
    public void resetValue(SettingsResetRequest req) {
        RedisUtils.deleteByPattern(CacheConstants.CONFIG_KEY_PREFIX + StringConstants.ASTERISK);
        String category = req.getCategory();
        List<String> codeList = req.getCode();
        ValidationUtils.throwIf(CharSequenceUtil.isBlank(category) && CollUtil.isEmpty(codeList), "键列表不能为空");
        LambdaUpdateChainWrapper<SettingsDO> updateWrapper = settingsMapper.lambdaUpdate().set(SettingsDO::getValue, null);
        if (CharSequenceUtil.isNotBlank(category)) {
            updateWrapper.eq(SettingsDO::getCategory, category);
        } else {
            updateWrapper.in(SettingsDO::getCode, req.getCode());
        }
        updateWrapper.update();
    }

    @Override
    public Boolean getBoolean(String key) {
        return settingsMapper.getBoolean(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = settingsMapper.getRawValue(key);
        return CharSequenceUtil.isBlank(value) ? defaultValue : Integer.parseInt(value);
    }

    @Override
    public <T> T getJsonBean(String key, Class<T> clazz) {
        String value = settingsMapper.getRawValue(key);
        try {
            return CharSequenceUtil.isBlank(value) ? null : objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            throw new JSONException(CharSequenceUtil.format("[系统配置] 读取 JOSN 值 [{}] 出错", key), e);
        }
    }

    @Override
    public <T> List<T> getBeanList(String key) {
        String value = settingsMapper.getRawValue(key);
        try {
            return CharSequenceUtil.isBlank(value) ? null : objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new JSONException(CharSequenceUtil.format("[系统配置]读取[{}]出错", key), e);
        }
    }

    @Override
    public JsonNode getJsonNode(String key) {
        String value = settingsMapper.getRawValue(key);
        try {
            return CharSequenceUtil.isBlank(value) ? null : objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new JSONException(CharSequenceUtil.format("[系统配置]读取[{}]出错", key), e);
        }
    }

    @Override
    public String getStringValue(String key) {
        return settingsMapper.getRawValue(key);
    }

    @Override
    @CacheEvict(value = CACHE_KEY_PREFIX, allEntries = true)
    public void refreshCache() {
        log.info("系统配置缓存已清空刷新");
    }

    @Override
    @Cacheable(value = CACHE_KEY_PREFIX, key = "'mail'")
    public MailAccount getMailAccount() {
        return this.getJsonBean("mail", MailAccount.class);
    }

    @Override
    public boolean isLoginCaptchaEnabled() {
        return settingsMapper.getBoolean("LOGIN_CAPTCHA_ENABLED");
    }

    @Override
    public int getMaxRetryCount() {
        return settingsMapper.getInt("MAX_RETRY_COUNT", 5);
    }

    @Override
    public int getLockMinutes() {
        return settingsMapper.getInt("LOCK_MINUTES", 5);
    }

    @Override
    public int getPasswordExpireDays() {
        return settingsMapper.getInt("PASSWORD_EXPIRE_DAYS",90);
    }
}
