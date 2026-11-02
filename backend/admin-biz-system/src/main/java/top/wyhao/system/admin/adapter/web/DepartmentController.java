package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.system.admin.app.service.DepartmentService;
import top.wyhao.cmn.core.model.Result;
import top.wyhao.web.core.model.IdResult;
import top.wyhao.web.core.model.IdsRequest;

import java.util.List;
import top.wyhao.system.admin.app.dto.DepartmentQuery;
import top.wyhao.system.admin.app.dto.DepartmentRequest;
import top.wyhao.system.admin.app.dto.DepartmentResult;

/**
 * 部门管理 API
 */
@Tag(name = "部门管理 API")
@RestController
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 查询树列表
     *
     * @param query 查询条件
     * @return 树列表信息
     */
    @Operation(summary = "查询树列表", description = "查询树列表")
    @GetMapping("/system/department/tree")
    public List<DepartmentResult> tree(@Valid DepartmentQuery query) {
        return departmentService.tree(query);
    }

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/system/department/{id}")
    public DepartmentResult get(@PathVariable Long id) {
        return departmentService.get(id);
    }

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return ID
     */
    @Operation(summary = "创建数据", description = "创建数据")
    @SaCheckPermission("system:department:create")
    @PostMapping("/system/department")
    public IdResult<Long> create(@RequestBody @Valid DepartmentRequest req) {
        return new IdResult<>(departmentService.create(req));
    }

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    @Operation(summary = "修改数据", description = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:department:update")
    @PatchMapping("/system/department/{id}")
    public void update(@RequestBody @Valid DepartmentRequest req, @PathVariable Long id) {
        departmentService.update(req, id);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:department:delete")
    @DeleteMapping("/system/department/{id}")
    public void delete(@PathVariable Long id) {
        departmentService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @Operation(summary = "批量删除数据", description = "批量删除数据")
    @SaCheckPermission("system:department:delete")
    @DeleteMapping("/system/department")
    public void batchDelete(@RequestBody @Valid IdsRequest req) {
        departmentService.delete(req.getIds());
    }

    /**
     * 导出
     *
     * @param query    查询条件
     * @param response 响应对象
     */
    @Operation(summary = "导出数据", description = "导出数据")
    @SaCheckPermission("system:department:export")
    @GetMapping("/system/department/export")
    public void export(@Valid DepartmentQuery query, HttpServletResponse response) {
        departmentService.export(query, response);
    }


    /**
     * 查询部门树
     *
     * @param query 查询条件
     * @return 树型字典列表信息
     */
    @Operation(summary = "查询部门树", description = "查询树型结构字典列表（树型结构下拉选项等场景）")
    @GetMapping("/dict/tree")
    public Result<List<DepartmentResult>> treeDict(@Valid DepartmentQuery query) {
        return Result.ok(departmentService.tree(query));
    }

    @Operation(summary = "查询部门字典树", description = "与部门树相同的列表，供下拉使用")
    @GetMapping("/system/department/dict/tree")
    public List<DepartmentResult> departmentDictTree(@Valid DepartmentQuery query) {
        return departmentService.tree(query);
    }
}