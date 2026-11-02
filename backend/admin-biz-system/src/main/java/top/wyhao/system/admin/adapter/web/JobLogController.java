package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.system.admin.app.dto.JobLogQuery;
import top.wyhao.system.admin.app.dto.vo.JobLogResponse;
import top.wyhao.system.admin.app.service.JobAdminService;
import top.wyhao.mybatis.query.PageResult;

@Tag(name = "定时任务日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule/log")
public class JobLogController {

    private final JobAdminService jobAdminService;

    @Operation(summary = "分页查询执行日志")
    @SaCheckPermission("schedule:log:list")
    @GetMapping
    public PageResult<JobLogResponse> page(@Valid JobLogQuery query) {
        return jobAdminService.pageLogs(query);
    }
}
