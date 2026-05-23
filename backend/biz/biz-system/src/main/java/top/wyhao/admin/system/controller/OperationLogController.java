
package top.wyhao.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.admin.system.model.query.LogQuery;
import top.wyhao.admin.system.model.vo.log.OperationLogDetailResult;
import top.wyhao.admin.system.model.vo.log.OperationLogResult;
import top.wyhao.admin.system.service.OperationLogService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;

/**
 * 操作日志 API
 *

 */
@Tag(name = "操作日志 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/log")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("monitor:log:list")
    @GetMapping
    public PageResult<OperationLogResult> page(@Valid LogQuery query, @Valid PageQuery pageQuery) {
        return operationLogService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("monitor:log:get")
    @GetMapping("/{id}")
    public OperationLogDetailResult get(@PathVariable Long id) {
        return operationLogService.detail(id);
    }

    @Operation(summary = "导出登录日志", description = "导出登录日志")
    @SaCheckPermission("monitor:log:export")
    @GetMapping("/export/login")
    public void exportLoginLog(@Valid LogQuery query, HttpServletResponse response) {
        operationLogService.exportLoginLog(query, response);
    }

    @Operation(summary = "导出操作日志", description = "导出操作日志")
    @SaCheckPermission("monitor:log:export")
    @GetMapping("/export/operation")
    public void exportOperationLog(@Valid LogQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        operationLogService.exportOperationLog(query, response);
    }
}
