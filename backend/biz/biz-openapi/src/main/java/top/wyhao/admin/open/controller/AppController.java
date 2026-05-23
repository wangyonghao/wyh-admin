package top.wyhao.admin.open.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.open.model.query.AppQuery;
import top.wyhao.admin.open.model.req.AppReq;
import top.wyhao.admin.open.model.resp.AppResp;
import top.wyhao.admin.open.service.AppService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.IdsRequest;
import top.wyhao.starter.web.core.model.IdResult;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 应用管理 API

 * @since 2023/12/24 22:56
 */
@Tag(name = "应用管理 API")
@RestController
@RequestMapping("/open/app")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @GetMapping
    public PageResult<AppResp> page(@Valid AppQuery query, @Valid PageQuery pageQuery) {
        return appService.findPage(query, pageQuery);
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
    public List<AppResp> list(@Valid AppQuery query, @Valid SortQuery sortQuery) {
        return appService.list(query, sortQuery);
    }

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return ID
     */
    @Operation(summary = "创建数据", description = "创建数据")
    @PostMapping
    public IdResult<Long> create(@RequestBody @Valid AppReq req) {
        return new IdResult<>(appService.create(req));
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
    public void update(@RequestBody @Valid AppReq req, @PathVariable Long id) {
        appService.update(req, id);
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
        appService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @Operation(summary = "批量删除数据", description = "批量删除数据")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsRequest req) {
        appService.delete(req.getIds());
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
    public void export(@Valid AppQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        appService.export(query, sortQuery, response);
    }
}