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

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.Cached;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.admin.system.model.enums.ConfigCategory;
import top.wyhao.admin.system.model.query.SettingsQuery;
import top.wyhao.admin.system.model.vo.file.FileUploadResp;
import top.wyhao.admin.system.service.DictService;
import top.wyhao.admin.system.service.FileService;
import top.wyhao.admin.system.service.SettingsService;
import top.wyhao.starter.tenant.context.TenantContextHolder;
import top.wyhao.starter.core.util.validation.ValidationUtils;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import java.io.IOException;
import java.util.List;

/**
 * 公共 API
 *
 * @author Charles7c
 * @since 2023/1/22 21:48
 */
@Tag(name = "公共 API")
@Validated
@RestController("systemCommonController")
@RequiredArgsConstructor
@RequestMapping("/system/common")
public class CommonController {

    private final FileService fileService;
    private final DictService dictService;
    private final SettingsService settingsService;

    @Operation(summary = "上传文件", description = "上传文件")
    @Parameter(name = "parentPath", description = "上级目录", example = "/", in = ParameterIn.QUERY)
    @PostMapping("/file")
    public FileUploadResp upload(@RequestPart @NotNull(message = "文件不能为空") MultipartFile file,
                                 @RequestParam(required = false) String parentPath) throws IOException {
        ValidationUtils.throwIf(file::isEmpty, "文件不能为空");
        FileInfo fileInfo = fileService.upload(file, parentPath);
        return FileUploadResp.builder()
            .id(fileInfo.getId())
            .url(fileInfo.getUrl())
            .thUrl(fileInfo.getThUrl())
            .metadata(fileInfo.getMetadata())
            .build();
    }

    @Operation(summary = "查询字典", description = "查询字典列表")
    @Parameter(name = "dictType", description = "字典类型", example = "notice_type", in = ParameterIn.PATH)
    @GetMapping("/dict/{dictType}")
    public List<LabelValueResp<String>> listDict(@PathVariable String dictType) {
        return dictService.listByDictType(dictType);
    }

    @SaIgnore
    @Operation(summary = "查询系统配置参数", description = "查询系统配置参数")
    @GetMapping("/dict/option/site")
    @Cached(key = "'SITE'", name = CacheConstants.CONFIG_KEY_PREFIX)
    public List<LabelValueResp<String>> listSiteConfigDict() {
        SettingsQuery settingsQuery = new SettingsQuery();
        settingsQuery.setCategory(ConfigCategory.SITE.name());
        return settingsService.list(settingsQuery)
            .stream()
            .map(config -> new LabelValueResp<>(config.getCode(), StrUtil.nullToDefault(config.getValue(), config
                .getDefaultValue())))
            .toList();
    }

    @SaIgnore
    @Operation(summary = "查询租户开启状态", description = "查询租户开启状态")
    @GetMapping("/dict/option/tenant")
    @Cached(key = "'TENANT'", name = CacheConstants.CONFIG_KEY_PREFIX)
    public Boolean tenantEnabled() {
        return TenantContextHolder.isTenantEnabled();
    }
}