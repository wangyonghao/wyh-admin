package top.wyhao.admin.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.query.LoginLogQuery;
import top.wyhao.admin.system.model.vo.log.LoginLogResult;
import top.wyhao.admin.system.service.LoginLogService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;

/**
 * 登录日志 API
 *

 * @since 2026/05/08
 */
@Tag(name = "登录日志 API")
@Validated
@RestController
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询", description = "分页查询登录日志列表")
    @GetMapping("/system/login-log")
    public PageResult<LoginLogResult> page(LoginLogQuery query, PageQuery pageQuery) {
        return loginLogService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情", description = "查询登录日志详情")
    @Parameter(name = "id", description = "ID", example = "1")
    @GetMapping("/system/login-log/{id}")
    public LoginLogResult get(@PathVariable Long id) {
        return loginLogService.detail(id);
    }

    @Operation(summary = "导出", description = "导出登录日志数据")
    @GetMapping("/system/login-log/export")
    public void export(LoginLogQuery query, HttpServletResponse response) {
        loginLogService.export(query, response);
    }
}
