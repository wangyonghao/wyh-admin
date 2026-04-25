package top.wyhao.admin.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.bo.DeptReq;
import top.wyhao.admin.system.model.query.DeptQuery;
import top.wyhao.admin.system.model.vo.DeptResp;
import top.wyhao.admin.system.service.DeptService;
import top.wyhao.starter.core.model.R;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.req.IdsReq;
import top.wyhao.starter.web.core.model.resp.IdResp;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;

import java.util.List;

/**
 * 部门管理 API
 */
@Tag(name = "部门管理 API")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @GetMapping
    public PageResult<DeptResp> page(@Valid DeptQuery query, @Valid PageQuery pageQuery) {
        return deptService.page(query, pageQuery);
    }

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @return 列表信息
     */
    @Operation(summary = "查询列表", description = "查询列表")
    @GetMapping("/list")
    public List<DeptResp> list(@Valid DeptQuery query) {
        return deptService.list(query);
    }

    /**
     * 查询树列表
     *
     * @param query     查询条件
     * @return 树列表信息
     */
    @Operation(summary = "查询树列表", description = "查询树列表")
    @GetMapping("/tree")
    public List<DeptResp> tree(@Valid DeptQuery query) {
        return deptService.tree(query);
    }

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/{id}")
    public DeptResp get(@PathVariable Long id) {
        return deptService.get(id);
    }

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return ID
     */
    @Operation(summary = "创建数据", description = "创建数据")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid DeptReq req) {
        return new IdResp<>(deptService.create(req));
    }

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    @Operation(summary = "修改数据", description = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid DeptReq req, @PathVariable Long id) {
        deptService.update(req, id);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        deptService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @Operation(summary = "批量删除数据", description = "批量删除数据")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        deptService.delete(req.getIds());
    }

    /**
     * 导出
     *
     * @param query     查询条件
     * @param response  响应对象
     */
    @Operation(summary = "导出数据", description = "导出数据")
    @GetMapping("/export")
    public void export(@Valid DeptQuery query, HttpServletResponse response) {
        deptService.export(query, response);
    }

    /**
     * 查询字典列表
     *
     * @param query     查询条件
     * @return 字典列表信息
     */
    @Operation(summary = "查询字典列表", description = "查询字典列表（下拉选项等场景）")
    @GetMapping("/dict")
    public List<LabelValueResp> dict(@Valid DeptQuery query) {
        return deptService.dict(query);
    }

    /**
     * 查询部门树
     *
     * @param query     查询条件
     * @return 树型字典列表信息
     */
    @Operation(summary = "查询部门树", description = "查询树型结构字典列表（树型结构下拉选项等场景）")
    @GetMapping("/dict/tree")
    public R<List<DeptResp>> treeDict(@Valid DeptQuery query) {
        return R.ok(deptService.tree(query));
    }
}