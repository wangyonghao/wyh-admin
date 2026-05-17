
package top.wyhao.admin.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.generator.model.entity.GenFieldConfig;
import top.wyhao.admin.generator.model.entity.GenConfig;
import top.wyhao.admin.generator.model.query.GenConfigQuery;
import top.wyhao.admin.generator.model.req.GenConfigReq;
import top.wyhao.admin.generator.model.resp.GeneratePreviewResp;
import top.wyhao.admin.generator.service.GeneratorService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.LabelValueResult;
import top.wyhao.starter.web.excel.DictApi;

import java.sql.SQLException;
import java.util.List;

/**
 * 代码生成 API
 */
@Tag(name = "代码生成 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/code/generator")
public class GeneratorController {

    private final GeneratorService baseService;
    private final DictApi dictApi;

    @Operation(summary = "分页查询生成配置", description = "分页查询生成配置列表")
    @SaCheckPermission("code:generator:list")
    @GetMapping("/config")
    public PageResult<GenConfig> pageGenConfig(@Valid GenConfigQuery query, @Valid PageQuery pageQuery) {
        return baseService.pageGenConfig(query, pageQuery);
    }

    @Operation(summary = "查询生成配置信息", description = "查询生成配置信息")
    @Parameter(name = "tableName", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("code:generator:list")
    @GetMapping("/config/{tableName}")
    public GenConfig getGenConfig(@PathVariable String tableName) throws SQLException {
        return baseService.getGenConfig(tableName);
    }

    @Operation(summary = "查询字段配置列表", description = "查询字段配置列表")
    @Parameter(name = "tableName", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @Parameter(name = "requireSync", description = "是否需要同步", example = "false", in = ParameterIn.QUERY)
    @SaCheckPermission("code:generator:config")
    @GetMapping("/field/{tableName}")
    public List<GenFieldConfig> listFieldConfig(@PathVariable String tableName,
                                               @RequestParam(required = false, defaultValue = "false") Boolean requireSync) {
        return baseService.listFieldConfig(tableName, requireSync);
    }

    @Operation(summary = "保存配置信息", description = "保存配置信息")
    @Parameter(name = "tableName", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("code:generator:config")
    @PostMapping("/config/{tableName}")
    public void saveConfig(@RequestBody @Valid GenConfigReq req, @PathVariable String tableName) {
        baseService.saveConfig(req, tableName);
    }

    @Operation(summary = "生成预览", description = "预览生成代码")
    @Parameter(name = "tableNames", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("code:generator:preview")
    @GetMapping("/preview/{tableNames}")
    public List<GeneratePreviewResp> preview(@PathVariable List<String> tableNames) {
        return baseService.preview(tableNames);
    }

    @Operation(summary = "生成下载代码", description = "生成下载代码")
    @Parameter(name = "tableNames", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("code:generator:generate")
    @PostMapping("/{tableNames}/download")
    public void downloadCode(@PathVariable List<String> tableNames, HttpServletResponse response) {
        baseService.downloadCode(tableNames, response);
    }

    @Operation(summary = "生成代码", description = "生成代码")
    @Parameter(name = "tableNames", description = "表名称", required = true, example = "sys_user", in = ParameterIn.PATH)
    @SaCheckPermission("code:generator:generate")
    @PostMapping("/{tableNames}")
    public void generateCode(@PathVariable List<String> tableNames) {
        baseService.generateCode(tableNames);
    }

    @Operation(summary = "查询字典", description = "查询字典列表（包含枚举字典）")
    @SaCheckPermission("code:generator:config")
    @GetMapping("/dict")
    public List<LabelValueResult> listDict() {
        return dictApi.listAll();
    }
}
