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
import top.wyhao.system.admin.app.dto.LoginLogQuery;
import top.wyhao.system.admin.app.dto.LoginLogResult;
import top.wyhao.system.admin.app.service.LoginLogService;
import top.wyhao.web.core.model.PageQuery;

/**
 * 登录日志 API（监控管理；落库由 Auth 事件触发）
 */
@Tag(name = "登录日志 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/login-log")
public class AuthenticationLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "查询登录日志", description = "分页查询登录日志列表")
    @GetMapping
    public PageResult<LoginLogResult> page(LoginLogQuery query, PageQuery pageQuery) {
        return loginLogService.page(query, pageQuery);
    }

    @Operation(summary = "导出登录日志", description = "导出登录日志数据")
    @SaCheckPermission("monitor:log:export")
    @GetMapping("/export")
    public void export(LoginLogQuery query, HttpServletResponse response) {
        loginLogService.export(query, response);
    }
}
