
package top.wyhao.system.admin.adapter.web;

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
import org.springframework.web.multipart.MultipartFile;
import top.wyhao.system.admin.app.dto.UserDetail;
import top.wyhao.system.admin.app.dto.UserQuery;
import top.wyhao.system.admin.app.dto.UserRequest;
import top.wyhao.system.admin.app.dto.UserResult;
import top.wyhao.system.admin.app.dto.UserImportRequest;
import top.wyhao.system.admin.app.dto.UserRoleUpdateReq;
import top.wyhao.system.admin.app.dto.UserImportParseResp;
import top.wyhao.system.admin.app.dto.UserImportResp;
import top.wyhao.system.admin.app.service.UserAdminService;
import top.wyhao.identity.client.PasswordApi;
import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.cmn.core.model.Result;
import top.wyhao.web.core.model.IdResult;
import top.wyhao.web.core.model.IdsRequest;
import top.wyhao.web.core.model.LabelValueResult;
import top.wyhao.web.util.HttpUtil;

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
public class AdminUserController {
    private final UserAdminService userAdminService;
    private final PasswordApi passwordApi;

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
    public PageResult<UserResult> page(@Valid UserQuery query, @Valid PageParam pageParam) {
        return userAdminService.page(query, pageParam);
    }

    /**
     * 查询用户详情
     *
     * @param id 用户id
     * @return 详情信息
     */
    @SaCheckPermission("system:user:get")
    @GetMapping("/{id}")
    public UserDetail get(@PathVariable Long id) {
        return userAdminService.detail(id);
    }

    /**
     * 创建新用户
     *
     * @param request 创建请求参数
     * @return ID
     */
    @SaCheckPermission("system:user:create")
    @PostMapping
    public IdResult<Long> create(@RequestBody @Validated(UserRequest.Create.class) UserRequest request) {
        return new IdResult<>(userAdminService.create(request));
    }

    /**
     * 修改
     *
     * @param request 修改请求参数
     * @param id  ID
     */
    @SaCheckPermission("system:user:update")
    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody @Validated(UserRequest.Update.class) UserRequest request) {
        userAdminService.update(id, request);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userAdminService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @SaCheckPermission("system:user:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsRequest req) {
        userAdminService.delete(req.getIds());
    }

    /**
     * 导入
     */
    @SaCheckPermission("system:user:import")
    @GetMapping("/import")
    public void importUsers(@Valid UserImportRequest userImportRequest) {
        userAdminService.importUser(userImportRequest);
    }

    @Operation(summary = "解析导入文件", description = "解析导入文件")
    @SaCheckPermission("system:user:import")
    @PostMapping("/import/parse")
    public UserImportParseResp parseImport(@RequestPart("file") MultipartFile file) {
        return userAdminService.parseImport(file);
    }

    @Operation(summary = "导入用户", description = "导入用户")
    @SaCheckPermission("system:user:import")
    @PostMapping("/import")
    public UserImportResp importUsersPost(@RequestBody @Valid UserImportRequest userImportRequest) {
        return userAdminService.importUser(userImportRequest);
    }

    @Operation(summary = "用户字典", description = "下拉选项")
    @SaCheckPermission("system:user:list")
    @GetMapping("/dict")
    public List<LabelValueResult<Long>> dict(UserQuery query) {
        return userAdminService.dict(query);
    }

    @Operation(summary = "分配角色", description = "分配角色")
    @SaCheckPermission("system:user:updateRole")
    @PutMapping("/{id}/role")
    public void updateRole(@PathVariable Long id, @RequestBody @Valid UserRoleUpdateReq request) {
        userAdminService.updateRole(request, id);
    }
    /**
     * 导出
     *
     * @param query     查询条件
     * @param response  响应对象
     */
    @SaCheckPermission("system:user:export")
    @GetMapping("/export")
    public void export(@Valid UserQuery query, HttpServletResponse response) {
        userAdminService.export(query, response);
    }

    @SaCheckPermission("system:user:export")
    @GetMapping({"/download-template", "/import/template"})
    public void downloadTemplate(HttpServletResponse response) {
        try {
            HttpUtil.writeAttachmentToResponse(ResourceUtil.getStream("templates/import/user.xlsx"), "用户导入模板.xlsx", response);
        } catch (Exception e) {
            log.error("下载用户导入模板失败：{}", e.getMessage(), e);
            response.setCharacterEncoding(CharsetUtil.UTF_8);
            response.setContentType(ContentType.JSON.toString());
            try {
                response.getWriter().write(JSONUtil.toJsonStr(Result.fail("DOWNLOAD_FAILED","下载用户导入模板失败")));
            } catch (IOException ex) {
                throw UserException.downloadTemplateFailed(e.getMessage());
            }
        }
    }

    /**
     * 重置密码
     *
     * @param id 用户ID
     * @return 新密码
     */
    @Operation(summary = "重置密码", description = "重置用户密码，返回新密码")
    @SaCheckPermission("system:user:resetPwd")
    @PutMapping("/{id}/password/reset")
    public String resetPassword(@PathVariable Long id) {
        return passwordApi.resetToRandomPassword(id);
    }
}