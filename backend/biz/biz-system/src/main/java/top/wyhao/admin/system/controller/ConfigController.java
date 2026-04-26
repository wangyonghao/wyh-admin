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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.bo.ConfigRequest;
import top.wyhao.admin.system.model.query.ConfigQuery;
import top.wyhao.admin.system.model.vo.ConfigResult;
import top.wyhao.admin.system.model.vo.config.*;
import top.wyhao.admin.system.service.ConfigService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.req.IdsReq;
import top.wyhao.starter.web.core.model.resp.IdResp;

import java.util.List;

/**
 * 系统配置 API
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Tag(name = "系统配置 API")
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    // ==================== 配置项专用接口 ====================

    /**
     * 获取站点配置
     */
    @Operation(summary = "获取站点配置")
    @GetMapping("/site")
    public SiteConfigVO getSiteConfig() {
        return configService.getSiteConfig();
    }

    /**
     * 更新站点配置
     */
    @Operation(summary = "更新站点配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/site")
    public void updateSiteConfig(@RequestBody @Valid SiteConfigVO config) {
        configService.updateSiteConfig(config);
    }

    /**
     * 获取登录配置
     */
    @Operation(summary = "获取登录配置")
    @GetMapping("/login")
    public LoginConfigVO getLoginConfig() {
        return configService.getLoginConfig();
    }

    /**
     * 更新登录配置
     */
    @Operation(summary = "更新登录配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/login")
    public void updateLoginConfig(@RequestBody @Valid LoginConfigVO config) {
        configService.updateLoginConfig(config);
    }

    /**
     * 获取注册配置
     */
    @Operation(summary = "获取注册配置")
    @GetMapping("/register")
    public RegisterConfigVO getRegisterConfig() {
        return configService.getRegisterConfig();
    }

    /**
     * 更新注册配置
     */
    @Operation(summary = "更新注册配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/register")
    public void updateRegisterConfig(@RequestBody @Valid RegisterConfigVO config) {
        configService.updateRegisterConfig(config);
    }

    /**
     * 获取邮件配置
     */
    @Operation(summary = "获取邮件配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/email")
    public EmailConfigVO getEmailConfig() {
        return configService.getEmailConfig();
    }

    /**
     * 更新邮件配置
     */
    @Operation(summary = "更新邮件配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/email")
    public void updateEmailConfig(@RequestBody @Valid EmailConfigVO config) {
        configService.updateEmailConfig(config);
    }

    /**
     * 获取短信配置
     */
    @Operation(summary = "获取短信配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/sms")
    public SmsConfigVO getSmsConfig() {
        return configService.getSmsConfig();
    }

    /**
     * 更新短信配置
     */
    @Operation(summary = "更新短信配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/sms")
    public void updateSmsConfig(@RequestBody @Valid SmsConfigVO config) {
        configService.updateSmsConfig(config);
    }

    /**
     * 获取存储配置
     */
    @Operation(summary = "获取存储配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/storage")
    public StorageConfigVO getStorageConfig() {
        return configService.getStorageConfig();
    }

    /**
     * 更新存储配置
     */
    @Operation(summary = "更新存储配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/storage")
    public void updateStorageConfig(@RequestBody @Valid StorageConfigVO config) {
        configService.updateStorageConfig(config);
    }

    /**
     * 获取安全配置
     */
    @Operation(summary = "获取安全配置")
    @GetMapping("/security")
    public SecurityConfigVO getSecurityConfig() {
        return configService.getSecurityConfig();
    }

    /**
     * 更新安全配置
     */
    @Operation(summary = "更新安全配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/security")
    public void updateSecurityConfig(@RequestBody @Valid SecurityConfigVO config) {
        configService.updateSecurityConfig(config);
    }

    // ==================== 通用 CRUD 接口 ====================

    /**
     * 分页查询列表
     */
    @Operation(summary = "分页查询列表")
    @SaCheckPermission("system:config:list")
    @GetMapping
    public PageResult<ConfigResult> page(@Valid ConfigQuery query, @Valid PageQuery pageQuery) {
        return configService.page(query, pageQuery);
    }

    /**
     * 查询详情
     */
    @Operation(summary = "查询详情")
    @SaCheckPermission("system:config:list")
    @GetMapping("/{id}")
    public ConfigResult get(@PathVariable Long id) {
        return configService.detail(id);
    }

    /**
     * 根据键查询配置
     */
    @Operation(summary = "根据键查询配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/key/{configKey}")
    public ConfigResult getByKey(@PathVariable String configKey) {
        return configService.getByKey(configKey);
    }

    /**
     * 新增
     */
    @Operation(summary = "新增")
    @SaCheckPermission("system:config:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Validated(ConfigRequest.Create.class) ConfigRequest request) {
        return new IdResp<>(configService.create(request));
    }

    /**
     * 修改
     */
    @Operation(summary = "修改")
    @SaCheckPermission("system:config:edit")
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id,
                       @RequestBody @Validated(ConfigRequest.Update.class) ConfigRequest request) {
        configService.update(id, request);
    }

    /**
     * 根据键更新配置
     */
    @Operation(summary = "根据键更新配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/key/{configKey}")
    public void updateByKey(@PathVariable String configKey,
                            @RequestBody @Validated(ConfigRequest.Update.class) ConfigRequest request) {
        configService.updateByKey(configKey, request);
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @SaCheckPermission("system:config:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        configService.delete(List.of(id));
    }

    /**
     * 批量删除
     */
    @Operation(summary = "批量删除")
    @SaCheckPermission("system:config:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        configService.delete(req.getIds());
    }

    /**
     * 导出
     */
    @Operation(summary = "导出")
    @SaCheckPermission("system:config:export")
    @GetMapping("/export")
    public void export(@Valid ConfigQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        configService.export(query, sortQuery, response);
    }
}
