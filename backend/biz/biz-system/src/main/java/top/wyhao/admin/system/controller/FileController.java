package top.wyhao.admin.system.controller;

import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.query.FileQuery;
import top.wyhao.admin.system.model.bo.FileReq;
import top.wyhao.admin.system.model.vo.file.FileResp;
import top.wyhao.admin.system.service.FileService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.req.IdsReq;
import top.wyhao.starter.web.core.model.resp.IdResp;
import top.wyhao.starter.web.core.model.resp.LabelValueResp;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 文件管理 API
 *
 * @author Charles7c
 * @since 2023/12/24 22:56
 */
@Tag(name = "文件管理 API")
@RestController
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @GetMapping
    public PageResult<FileResp> page(@Valid FileQuery query, @Valid PageQuery pageQuery) {
        return fileService.findPage(query, pageQuery);
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
    public List<FileResp> list(@Valid FileQuery query, @Valid SortQuery sortQuery) {
        return fileService.list(query, sortQuery);
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
    public List<Tree<Long>> tree(@Valid FileQuery query, @Valid SortQuery sortQuery) {
        return fileService.tree(query, sortQuery, false);
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
    public FileResp get(@PathVariable("id") Long id) {
        return fileService.get(id);
    }

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return ID
     */
    @Operation(summary = "创建数据", description = "创建数据")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid FileReq req) {
        return new IdResp<>(fileService.create(req));
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
    public void update(@RequestBody @Valid FileReq req, @PathVariable("id") Long id) {
        fileService.update(req, id);
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
        fileService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @Operation(summary = "批量删除数据", description = "批量删除数据")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        fileService.delete(req.getIds());
    }

    /**
     * 导出
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    @Operation(summary = "导出数据", description = "导出数据")
    @GetMapping("/export")
    public void export(@Valid FileQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        fileService.export(query, sortQuery, response);
    }

    /**
     * 查询字典列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 字典列表信息
     */
    @Operation(summary = "查询字典列表", description = "查询字典列表（下拉选项等场景）")
    @GetMapping("/dict")
    public List<LabelValueResp> dict(@Valid FileQuery query, @Valid SortQuery sortQuery) {
        return fileService.dict(query, sortQuery);
    }

    /**
     * 查询树型字典列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 树型字典列表信息
     */
    @Operation(summary = "查询树型字典列表", description = "查询树型结构字典列表（树型结构下拉选项等场景）")
    @GetMapping("/dict/tree")
    public List<Tree<Long>> treeDict(@Valid FileQuery query, @Valid SortQuery sortQuery) {
        return fileService.tree(query, sortQuery, true);
    }
}