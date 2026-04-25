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

import com.fasterxml.jackson.databind.JsonNode;
import top.wyhao.admin.system.model.enums.ConfigCategory;
import top.wyhao.admin.system.model.query.SettingsQuery;
import top.wyhao.admin.system.model.bo.SettingsRequest;
import top.wyhao.admin.system.model.bo.SettingsResetRequest;
import top.wyhao.admin.system.model.vo.SettingsResult;
import top.wyhao.starter.mail.MailConfigurer;

import java.util.List;
import java.util.Map;

/**
 * 系统配置业务接口
 */
public interface SettingsService extends MailConfigurer {

    /**
     * 查询列表
     *
     * @param query 查询条件
     * @return 列表信息
     */
    List<SettingsResult> list(SettingsQuery query);

    void updateJsonValue(String key, Object value);

    void updateValue(String key, String value);

    /**
     * 根据类别查询
     *
     * @param category 类别
     * @return 参数信息
     */
    Map<String, String> getByCategory(ConfigCategory category);

    /**
     * 添加一项配置
     * @param config
     */
    void add(SettingsRequest config);

    /**
     * 修改参数
     *
     * @param configs 参数列表
     */
    void update(List<SettingsRequest> configs);

    /**
     * 重置参数
     *
     * @param req 重置信息
     */
    void resetValue(SettingsResetRequest req);
    
    Boolean getBoolean(String key);
    
    /**
     * 获取整数类型配置值
     *
     * @param key 键
     * @param defaultValue 默认值
     * @return 整数值
     */
    int getInt(String key, int defaultValue);
    
    /**
     * 读取 JSON 类型的配置项
     */
    <T> T getJsonBean(String key, Class<T> clazz);

    /**
     * 读取 JSON Array类型的配置项
     */
    <T> List<T> getBeanList(String key);

    JsonNode getJsonNode(String key);

    /**
     * 读取 String 类型的配置项
     */
    String getStringValue(String key);

    /**
     * 刷新系统配置缓存
     */
    void refreshCache();

    boolean isLoginCaptchaEnabled();

    int getMaxRetryCount();

    int getLockMinutes();

    int getPasswordExpireDays();
}
