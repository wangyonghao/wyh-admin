package top.wyhao.system.admin.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.system.admin.app.service.SmsService;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;

import java.util.List;
import top.wyhao.system.admin.app.dto.SmsLogQuery;
import top.wyhao.system.admin.app.dto.SmsLogResult;

/**
 * 短信日志管理 API
 *
 * @since 2023/12/24 22:56
 */
@Tag(name = "短信日志管理 API")
@RestController
@RequiredArgsConstructor
public class SmsLogController {

    private final SmsService smsService;

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @GetMapping("/system/sms/log")
    public PageResult<SmsLogResult> page(@Valid SmsLogQuery query, @Valid PageQuery pageQuery) {
        return smsService.page(query, pageQuery);
    }

    /**
     * 查询列表
     *
     * @param query 查询条件
     * @return 列表信息
     */
    @Operation(summary = "查询列表", description = "查询列表")
    @GetMapping("/system/sms/log/list")
    public List<SmsLogResult> list(@Valid SmsLogQuery query) {
        return smsService.list(query);
    }

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/system/sms/log/{id}")
    public SmsLogResult get(@PathVariable Long id) {
        return smsService.get(id);
    }

    /**
     * 导出
     *
     * @param query    查询条件
     * @param response 响应对象
     */
    @Operation(summary = "导出数据", description = "导出数据")
    @GetMapping("/system/sms/log/export")
    public void export(@Valid SmsLogQuery query, HttpServletResponse response) {
        smsService.export(query, response);
    }
}