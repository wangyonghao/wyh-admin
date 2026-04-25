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
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.bo.user.UserImportReq;
import top.wyhao.admin.system.model.bo.user.UserRequest;
import top.wyhao.admin.system.model.query.UserQuery;
import top.wyhao.admin.system.model.vo.user.UserDetailResult;
import top.wyhao.admin.system.model.vo.user.UserResult;
import top.wyhao.admin.system.service.UserService;
import top.wyhao.starter.core.exception.SystemException;
import top.wyhao.starter.core.model.R;
import top.wyhao.starter.core.util.FileUploadUtils;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.req.IdsReq;
import top.wyhao.starter.web.core.model.resp.IdResp;

import java.io.IOException;
import java.util.List;

/**
 * 用户管理 API
 */
@Tag(name="用户管理 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class UserController {
    private final UserService userService;

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("system:user:list")
    @GetMapping
    public PageResult<UserResult> page(@Valid UserQuery query, @Valid PageQuery pageQuery) {
        return userService.page(query, pageQuery);
    }

    /**
     * 查询用户详情
     *
     * @param id 用户id
     * @return 详情信息
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/{id}")
    public UserDetailResult get(@PathVariable Long id) {
        return userService.detail(id);
    }

    /**
     * 创建新用户
     *
     * @param request 创建请求参数
     * @return ID
     */
    @SaCheckPermission("system:user:create")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Validated(UserRequest.Create.class) UserRequest request) {
        return new IdResp<>(userService.create(request));
    }

    /**
     * 修改
     *
     * @param request 修改请求参数
     * @param id  ID
     */
    @SaCheckPermission("system:user:edit")
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody @Validated(UserRequest.Update.class) UserRequest request) {
        userService.update(id, request);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @SaCheckPermission("system:user:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        userService.delete(req.getIds());
    }

    /**
     * 导入
     */
    @SaCheckPermission("system:user:import")
    @GetMapping("/import")
    public void importUsers(@Valid UserImportReq userImportReq) {
        userService.importUser(userImportReq);
    }
    /**
     * 导出
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    @SaCheckPermission("system:user:export")
    @GetMapping("/export")
    public void export(@Valid UserQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        userService.export(query, sortQuery, response);
    }

    @SaCheckPermission("system:user:export")
    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            FileUploadUtils.download(response, ResourceUtil.getStream("templates/import/user.xlsx"), "用户导入模板.xlsx");
        } catch (Exception e) {
            log.error("下载用户导入模板失败：{}", e.getMessage(), e);
            response.setCharacterEncoding(CharsetUtil.UTF_8);
            response.setContentType(ContentType.JSON.toString());
            try {
                response.getWriter().write(JSONUtil.toJsonStr(R.fail("DOWNLOAD_FAILED","下载用户导入模板失败")));
            } catch (IOException ex) {
                throw new SystemException("下载用户导入模板失败：" + e.getMessage(), ex);
            }
        }
    }
}