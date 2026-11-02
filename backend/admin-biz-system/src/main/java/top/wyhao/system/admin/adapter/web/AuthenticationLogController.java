package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.auth.app.dto.AuthenticationLogQuery;
import top.wyhao.system.auth.app.dto.AuthenticationLogResult;
import top.wyhao.system.auth.app.service.AuthLogService;
import top.wyhao.web.core.model.PageQuery;

/**
 * 登录日志 API（监控管理，与认证入口解耦）
 */
@Tag(name = "登录日志 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/login-log")
public class AuthenticationLogController {

    private final AuthLogService authLogService;

    @Operation(summary = "查询登录日志", description = "分页查询登录日志列表")
    @GetMapping
    public PageResult<AuthenticationLogResult> page(AuthenticationLogQuery query, PageQuery pageQuery) {
        return authLogService.page(query, pageQuery);
    }

    @Operation(summary = "导出登录日志", description = "导出登录日志数据")
    @SaCheckPermission("monitor:log:export")
    @GetMapping("/export")
    public void export(AuthenticationLogQuery query, HttpServletResponse response) {
        authLogService.export(query, response);
    }
}
