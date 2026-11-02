package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.system.admin.app.dto.JobQuery;
import top.wyhao.system.admin.app.dto.JobSaveRequest;
import top.wyhao.system.admin.app.dto.JobStatusRequest;
import top.wyhao.system.admin.app.dto.vo.JobHandlerResponse;
import top.wyhao.system.admin.app.dto.vo.JobResponse;
import top.wyhao.system.admin.app.service.JobAdminService;
import top.wyhao.mybatis.query.PageResult;

import java.util.List;

@Tag(name = "定时任务")
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule/job")
public class JobController {

    private final JobAdminService jobAdminService;

    @Operation(summary = "已注册任务目录")
    @SaCheckPermission("schedule:job:list")
    @GetMapping("/handlers")
    public List<JobHandlerResponse> handlers() {
        return jobAdminService.listHandlers();
    }

    @Operation(summary = "分页查询任务")
    @SaCheckPermission("schedule:job:list")
    @GetMapping
    public PageResult<JobResponse> page(@Valid JobQuery query) {
        return jobAdminService.page(query);
    }

    @Operation(summary = "任务详情")
    @SaCheckPermission("schedule:job:get")
    @GetMapping("/{id}")
    public JobResponse detail(@PathVariable Long id) {
        return jobAdminService.detail(id);
    }

    @Operation(summary = "新增任务")
    @SaCheckPermission("schedule:job:create")
    @PostMapping
    public void create(@RequestBody @Valid JobSaveRequest request) {
        jobAdminService.create(request);
    }

    @Operation(summary = "修改任务")
    @SaCheckPermission("schedule:job:update")
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody @Valid JobSaveRequest request) {
        jobAdminService.update(id, request);
    }

    @Operation(summary = "激活或停止")
    @SaCheckPermission("schedule:job:update")
    @PatchMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestBody @Valid JobStatusRequest request) {
        jobAdminService.updateStatus(id, request.getStatus());
    }

    @Operation(summary = "删除任务")
    @SaCheckPermission("schedule:job:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        jobAdminService.delete(id);
    }

    @Operation(summary = "立即执行一次")
    @SaCheckPermission("schedule:job:trigger")
    @PostMapping("/{id}/trigger")
    public void trigger(@PathVariable Long id) {
        jobAdminService.trigger(id);
    }

    @Operation(summary = "立即执行一次（兼容旧路径）")
    @SaCheckPermission("schedule:job:trigger")
    @PostMapping("/trigger/{id}")
    public void triggerLegacy(@PathVariable Long id) {
        jobAdminService.trigger(id);
    }
}
