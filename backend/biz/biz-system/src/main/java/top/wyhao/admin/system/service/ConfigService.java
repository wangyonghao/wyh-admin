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

import jakarta.servlet.http.HttpServletResponse;
import top.wyhao.admin.system.model.bo.ConfigRequest;
import top.wyhao.admin.system.model.query.ConfigQuery;
import top.wyhao.admin.system.model.vo.ConfigResult;
import top.wyhao.admin.system.model.vo.config.*;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

import java.util.List;

/**
 * 系统配置业务接口
 *
 * @author Yonghao Wang
 * @since 2024/04/26
 */
public interface ConfigService {

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResult<ConfigResult> page(ConfigQuery query, PageQuery pageQuery);

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    ConfigResult detail(Long id);

    /**
     * 根据键查询配置
     *
     * @param configKey 配置键
     * @return 配置信息
     */
    ConfigResult getByKey(String configKey);

    /**
     * 获取站点配置
     *
     * @return 站点配置
     */
    SiteConfigVO getSiteConfig();

    /**
     * 更新站点配置
     *
     * @param config 站点配置
     */
    void updateSiteConfig(SiteConfigVO config);

    /**
     * 获取登录配置
     *
     * @return 登录配置
     */
    LoginConfigVO getLoginConfig();

    /**
     * 更新登录配置
     *
     * @param config 登录配置
     */
    void updateLoginConfig(LoginConfigVO config);

    /**
     * 获取注册配置
     *
     * @return 注册配置
     */
    RegisterConfigVO getRegisterConfig();

    /**
     * 更新注册配置
     *
     * @param config 注册配置
     */
    void updateRegisterConfig(RegisterConfigVO config);

    /**
     * 获取邮件配置
     *
     * @return 邮件配置
     */
    EmailConfigVO getEmailConfig();

    /**
     * 更新邮件配置
     *
     * @param config 邮件配置
     */
    void updateEmailConfig(EmailConfigVO config);

    /**
     * 获取短信配置
     *
     * @return 短信配置
     */
    SmsConfigVO getSmsConfig();

    /**
     * 更新短信配置
     *
     * @param config 短信配置
     */
    void updateSmsConfig(SmsConfigVO config);

    /**
     * 获取存储配置
     *
     * @return 存储配置
     */
    StorageConfigVO getStorageConfig();

    /**
     * 更新存储配置
     *
     * @param config 存储配置
     */
    void updateStorageConfig(StorageConfigVO config);

    /**
     * 获取安全配置
     *
     * @return 安全配置
     */
    SecurityConfigVO getSecurityConfig();

    /**
     * 更新安全配置
     *
     * @param config 安全配置
     */
    void updateSecurityConfig(SecurityConfigVO config);

    /**
     * 新增
     *
     * @param request 创建信息
     * @return ID
     */
    Long create(ConfigRequest request);

    /**
     * 修改
     *
     * @param id      ID
     * @param request 修改信息
     */
    void update(Long id, ConfigRequest request);

    /**
     * 根据键更新配置
     *
     * @param configKey 配置键
     * @param request   修改信息
     */
    void updateByKey(String configKey, ConfigRequest request);

    /**
     * 删除
     *
     * @param ids ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 导出
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    void export(ConfigQuery query, SortQuery sortQuery, HttpServletResponse response);
}
