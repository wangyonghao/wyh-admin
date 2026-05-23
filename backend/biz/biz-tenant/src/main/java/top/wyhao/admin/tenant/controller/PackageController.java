package top.wyhao.admin.tenant.controller;

import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.tenant.model.query.PackageQuery;
import top.wyhao.admin.tenant.model.req.PackageReq;
import top.wyhao.admin.tenant.model.resp.PackageDetailResp;
import top.wyhao.admin.tenant.model.resp.PackageResp;
import top.wyhao.admin.tenant.service.PackageService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.IdsRequest;
import top.wyhao.starter.web.core.model.IdResult;

import java.util.List;

/**
 * 套餐管理 API
 *

 * @since 2023/12/24 22:56
 */
@Tag(name = "套餐管理 API")
@RestController
@RequestMapping("/tenant/package")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService baseService;

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @GetMapping
    public PageResult<PackageResp> page(@Valid PackageQuery query, @Valid PageQuery pageQuery) {
        return baseService.findPage(query, pageQuery);
    }

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 列表信息
     */
    @Operation(summary = "查询列表", description = "查询列表")
    @GetMapping("/list")
    public List<PackageResp> list(@Valid PackageQuery query, @Valid SortQuery sortQuery) {
        return baseService.list(query, sortQuery);
    }

    /**
     * 查询树列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 树列表信息
     */
    @Operation(summary = "查询树列表", description = "查询树列表")
    @GetMapping("/tree")
    public List<Tree<Long>> tree(@Valid PackageQuery query, @Valid SortQuery sortQuery) {
        return baseService.tree(query, sortQuery, false);
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
    public PackageDetailResp get(@PathVariable("id") Long id) {
        return baseService.get(id);
    }

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return ID
     */
    @Operation(summary = "创建数据", description = "创建数据")
    @PostMapping
    public IdResult<Long> create(@RequestBody @Valid PackageReq req) {
        return new IdResult<>(baseService.create(req));
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
    public void update(@RequestBody @Valid PackageReq req, @PathVariable("id") Long id) {
        baseService.update(req, id);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        baseService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @Operation(summary = "批量删除数据", description = "批量删除数据")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsRequest req) {
        baseService.delete(req.getIds());
    }

}