
package top.wyhao.admin.schedule.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.schedule.model.query.JobQuery;
import top.wyhao.admin.schedule.model.req.JobReq;
import top.wyhao.admin.schedule.model.req.JobStatusReq;
import top.wyhao.admin.schedule.model.req.JobTriggerReq;
import top.wyhao.admin.schedule.model.resp.JobResp;
import top.wyhao.admin.schedule.service.JobService;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 任务 API


 * @since 2024/6/25 22:24
 */
@Tag(name = " 任务 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule/job")
public class JobController {

    private final JobService baseService;

    @Operation(summary = "分页查询任务列表", description = "分页查询任务列表")
    @SaCheckPermission("schedule:job:list")
    @GetMapping
    public PageResult<JobResp> page(JobQuery query) {
        return baseService.page(query);
    }

    @Operation(summary = "新增任务", description = "新增任务")
    @SaCheckPermission("schedule:job:create")
    @PostMapping
    public void create(@RequestBody @Valid JobReq req) {
        BizAssert.isTrue(!baseService.create(req), "任务创建失败");
    }

    @Operation(summary = "修改任务", description = "修改任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid JobReq req, @PathVariable Long id) {
        BizAssert.isTrue(!baseService.update(req, id), "任务修改失败");
    }

    @Operation(summary = "修改任务状态", description = "修改任务状态")
    @SaCheckPermission("schedule:job:update")
    @PatchMapping("/{id}/status")
    public void updateStatus(@RequestBody @Valid JobStatusReq req, @PathVariable Long id) {
        BizAssert.isTrue(!baseService.updateStatus(req, id), "任务状态修改失败");
    }

    @Operation(summary = "删除任务", description = "删除任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        BizAssert.isTrue(!baseService.delete(id), "任务删除失败");
    }

    @Operation(summary = "执行任务", description = "执行任务")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("schedule:job:trigger")
    @PostMapping("/trigger/{id}")
    public void trigger(@PathVariable Long id) {
        JobTriggerReq req = new JobTriggerReq();
        req.setJobId(id);
        BizAssert.isTrue(!baseService.trigger(req), "任务执行失败");
    }

    @Operation(summary = "查询任务分组列表", description = "查询任务分组列表")
    @SaCheckPermission("schedule:job:list")
    @GetMapping("/group")
    public List<String> listGroup() {
        return baseService.listGroup();
    }
}
