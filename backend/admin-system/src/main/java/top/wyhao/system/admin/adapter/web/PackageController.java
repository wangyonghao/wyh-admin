package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.system.admin.app.dto.PackageQuery;
import top.wyhao.system.admin.app.dto.PackageRequest;
import top.wyhao.system.admin.app.dto.PackageDetailResp;
import top.wyhao.system.admin.app.dto.PackageResp;
import top.wyhao.system.admin.app.service.PackageService;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.web.core.model.IdsRequest;
import top.wyhao.web.core.model.IdResult;

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
    @SaCheckPermission("tenant:package:list")
    @GetMapping
    public PageResult<PackageResp> page(@Valid PackageQuery query, @Valid PageQuery pageQuery) {
        return baseService.findPage(query, pageQuery);
    }

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @return 列表信息
     */
    @Operation(summary = "查询列表", description = "查询列表")
    @GetMapping("/list")
    public List<PackageResp> list(@Valid PackageQuery query) {
        return baseService.list(query);
    }

    /**
     * 查询树列表
     *
     * @param query     查询条件
     * @return 树列表信息
     */
    @Operation(summary = "查询树列表", description = "查询树列表")
    @GetMapping("/tree")
    public List<Tree<Long>> tree(@Valid PackageQuery query) {
        return baseService.tree(query, false);
    }

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:package:get")
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
    @SaCheckPermission("tenant:package:create")
    @PostMapping
    public IdResult<Long> create(@RequestBody @Valid PackageRequest req) {
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
    @SaCheckPermission("tenant:package:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Valid PackageRequest req, @PathVariable("id") Long id) {
        baseService.update(req, id);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:package:delete")
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
    @SaCheckPermission("tenant:package:delete")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsRequest req) {
        baseService.delete(req.getIds());
    }

}