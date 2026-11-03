
package top.wyhao.system.admin.adapter.web;

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
import top.wyhao.system.admin.app.service.OperationLogService;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.OperationLogDetailResult;
import top.wyhao.system.admin.app.dto.OperationLogQuery;
import top.wyhao.system.admin.app.dto.OperationLogResult;

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
    public PageResult<OperationLogResult> page(@Valid OperationLogQuery query, @Valid PageQuery pageQuery) {
        return operationLogService.page(query, pageQuery);
    }

    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("monitor:log:get")
    @GetMapping("/{id}")
    public OperationLogDetailResult get(@PathVariable Long id) {
        return operationLogService.detail(id);
    }


    @Operation(summary = "导出操作日志", description = "导出操作日志")
    @SaCheckPermission("monitor:log:export")
    @GetMapping("/export/operation")
    public void export(@Valid OperationLogQuery query, HttpServletResponse response) {
        operationLogService.export(query, response);
    }
}
