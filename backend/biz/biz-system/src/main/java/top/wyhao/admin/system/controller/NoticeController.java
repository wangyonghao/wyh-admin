package top.wyhao.admin.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.query.NoticeQuery;
import top.wyhao.admin.system.model.bo.NoticeReq;
import top.wyhao.admin.system.model.vo.notice.NoticeDetailResp;
import top.wyhao.admin.system.model.vo.notice.NoticeResp;
import top.wyhao.admin.system.service.NoticeService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.req.IdsReq;
import top.wyhao.starter.web.core.model.resp.IdResp;
import top.wyhao.starter.web.core.model.PageResult;

import java.util.List;

/**
 * 公告管理 API
 *
 * @author Charles7c
 * @since 2023/12/24 22:56
 */
@Tag(name = "公告管理 API")
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @GetMapping
    public PageResult<NoticeResp> page(@Valid NoticeQuery query, @Valid PageQuery pageQuery) {
        return noticeService.findPage(query, pageQuery);
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
    public List<NoticeResp> list(@Valid NoticeQuery query, @Valid SortQuery sortQuery) {
        return noticeService.list(query, sortQuery);
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
    public NoticeDetailResp get(@PathVariable("id") Long id) {
        return noticeService.get(id);
    }

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return ID
     */
    @Operation(summary = "创建数据", description = "创建数据")
    @PostMapping
    public IdResp<Long> create(@RequestBody @Valid NoticeReq req) {
        return new IdResp<>(noticeService.create(req));
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
    public void update(@RequestBody @Valid NoticeReq req, @PathVariable("id") Long id) {
        noticeService.update(req, id);
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
        noticeService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @Operation(summary = "批量删除数据", description = "批量删除数据")
    @DeleteMapping
    public void batchDelete(@RequestBody @Valid IdsReq req) {
        noticeService.delete(req.getIds());
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
    public void export(@Valid NoticeQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        noticeService.export(query, sortQuery, response);
    }
}