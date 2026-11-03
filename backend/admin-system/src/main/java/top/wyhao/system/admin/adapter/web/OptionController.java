package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.mybatis.query.PageParam;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.OptionQuery;
import top.wyhao.system.admin.app.dto.OptionRequest;
import top.wyhao.system.admin.app.dto.OptionResult;
import top.wyhao.system.admin.app.service.OptionService;
import top.wyhao.web.core.model.IdResult;
import top.wyhao.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 选项管理 API
 */
@Tag(name = "选项管理 API")
@RestController
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("system:option:list")
    @GetMapping("/system/option/page")
    public PageResult<OptionResult> page(@Valid OptionQuery query, @Valid PageParam pageParam) {
        return optionService.page(query, pageParam);
    }

    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:option:get")
    @GetMapping("/system/option/detail/{id}")
    public OptionResult detail(@PathVariable Long id) {
        return optionService.detail(id);
    }

    @Operation(summary = "新增", description = "新增")
    @SaCheckPermission("system:option:create")
    @PostMapping("/system/option")
    public IdResult<Long> create(@Valid @RequestBody OptionRequest request) {
        return new IdResult<>(optionService.create(request));
    }

    @Operation(summary = "修改", description = "修改")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:option:update")
    @PutMapping("/system/option/{id}")
    public void update(@PathVariable Long id, @Valid @RequestBody OptionRequest request) {
        optionService.update(id, request);
    }

    @Operation(summary = "批量删除", description = "批量删除")
    @SaCheckPermission("system:option:delete")
    @DeleteMapping("/system/option")
    public void delete(@RequestBody List<Long> ids) {
        optionService.delete(ids);
    }

    @Operation(summary = "清除缓存", description = "清除指定类型的缓存")
    @Parameter(name = "type", description = "选项类型", example = "notice_type", in = ParameterIn.PATH)
    @SaCheckPermission("system:option:clearCache")
    @DeleteMapping("/system/option/cache/{type}")
    public void clearCache(@PathVariable String type) {
        optionService.clearCache(type);
    }

    @Operation(summary = "查询选项", description = "按类型查询已启用的选项")
    @Parameter(name = "type", description = "选项类型", example = "notice_type", in = ParameterIn.PATH)
    @GetMapping("/system/option/{type}")
    public List<LabelValueResult<String>> list(@PathVariable String type) {
        return optionService.listByType(type);
    }
}
