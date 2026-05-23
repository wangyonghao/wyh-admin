
package top.wyhao.admin.schedule.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.schedule.model.query.JobLogQuery;
import top.wyhao.admin.schedule.model.resp.JobLogResp;
import top.wyhao.admin.schedule.service.JobLogService;
import top.wyhao.starter.web.core.model.PageResult;

/**
 * 任务日志 API


 * @since 2024/6/27 22:24
 */
@Tag(name = " 任务日志 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule/log")
public class JobLogController {

    private final JobLogService baseService;

    @Operation(summary = "分页查询任务日志列表", description = "分页查询任务日志列表")
    @SaCheckPermission("schedule:log:list")
    @GetMapping
    public PageResult<JobLogResp> page(@Valid JobLogQuery query) {
        return baseService.page(query);
    }

    @Operation(summary = "停止任务", description = "停止任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:log:stop")
    @PostMapping("/stop/{id}")
    public void stop(@PathVariable Long id) {
        baseService.stop(id);
    }

    @Operation(summary = "重试任务", description = "重试任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:log:retry")
    @PostMapping("/retry/{id}")
    public void retry(@PathVariable Long id) {
        baseService.retry(id);
    }
}
