package top.wyhao.admin.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.bo.NoticeRequest;
import top.wyhao.admin.system.model.query.NoticeQuery;
import top.wyhao.admin.system.model.vo.NoticeDetailResult;
import top.wyhao.admin.system.model.vo.NoticeResult;
import top.wyhao.admin.system.service.NoticeService;
import top.wyhao.starter.web.core.model.PageQuery;
import top.wyhao.starter.web.core.model.PageResult;
import top.wyhao.starter.web.core.model.IdsRequest;
import top.wyhao.starter.web.core.model.IdResult;

import java.util.List;

/**
 * 公告管理 API
 *

 * @since 2026/5/8
 */
@Tag(name = "公告管理 API")
@RestController
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
    @GetMapping("/system/notice")
    public PageResult<NoticeResult> page(@Valid NoticeQuery query, @Valid PageQuery pageQuery) {
        return noticeService.page(query, pageQuery);
    }

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/system/notice/{id}")
    public NoticeDetailResult detail(@PathVariable Long id) {
        return noticeService.detail(id);
    }

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return ID
     */
    @Operation(summary = "创建数据", description = "创建数据")
    @PostMapping("/system/notice")
    public IdResult<Long> create(@RequestBody @Valid NoticeRequest req) {
        return new IdResult<>(noticeService.create(req));
    }

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    @Operation(summary = "修改数据", description = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @PutMapping("/system/notice/{id}")
    public void update(@RequestBody @Valid NoticeRequest req, @PathVariable Long id) {
        noticeService.update(req, id);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @DeleteMapping("/system/notice/{id}")
    public void delete(@PathVariable Long id) {
        noticeService.delete(List.of(id));
    }

    /**
     * 批量删除
     *
     * @param req 删除请求参数
     */
    @Operation(summary = "批量删除数据", description = "批量删除数据")
    @DeleteMapping("/system/notice")
    public void batchDelete(@RequestBody @Valid IdsRequest req) {
        noticeService.delete(req.getIds());
    }
}