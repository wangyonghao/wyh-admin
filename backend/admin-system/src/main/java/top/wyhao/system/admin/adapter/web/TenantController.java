package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wyhao.system.admin.app.dto.TenantQuery;
import top.wyhao.system.admin.app.dto.TenantRequest;
import top.wyhao.system.admin.app.dto.TenantDetailResp;
import top.wyhao.system.admin.app.dto.TenantResp;
import top.wyhao.system.admin.app.service.TenantService;
import top.wyhao.web.core.model.PageQuery;
import top.wyhao.web.core.model.IdResult;
import top.wyhao.web.core.model.LabelValueResult;
import top.wyhao.mybatis.query.PageResult;

import java.util.List;

/**
 * 租户管理 API
 *
 */
@Tag(name = "租户管理 API")
@RestController
@RequestMapping("/tenant/management")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * 分页查询列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("tenant:management:list")
    @GetMapping
    public PageResult<TenantResp> page(@Valid TenantQuery query, @Valid PageQuery pageQuery) {
        return tenantService.findPage(query, pageQuery);
    }

    /**
     * 查询列表
     *
     * @param query     查询条件
     * @return 列表信息
     */
    @Operation(summary = "查询列表", description = "查询列表")
    @GetMapping("/list")
    public List<TenantResp> list(@Valid TenantQuery query) {
        return tenantService.list(query);
    }

    /**
     * 查询详情
     *
     * @param id ID
     * @return 详情信息
     */
    @Operation(summary = "查询详情", description = "查询详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:management:get")
    @GetMapping("/{id}")
    public TenantDetailResp get(@PathVariable("id") Long id) {
        return tenantService.get(id);
    }

    /**
     * 创建
     *
     * @param req 创建请求参数
     * @return ID
     */
    @Operation(summary = "创建数据", description = "创建数据")
    @SaCheckPermission("tenant:management:create")
    @PostMapping
    public IdResult<Long> create(@RequestBody @Validated(TenantRequest.Create.class) TenantRequest req) {
        return new IdResult<>(tenantService.create(req));
    }

    /**
     * 修改
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    @Operation(summary = "修改数据", description = "修改数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:management:update")
    @PutMapping("/{id}")
    public void update(@RequestBody @Validated(TenantRequest.Update.class) TenantRequest req, @PathVariable("id") Long id) {
        tenantService.update(req, id);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("tenant:management:delete")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        tenantService.delete(List.of(id));
    }


    /**
     * 查询字典列表
     *
     * @param query     查询条件
     * @return 字典列表信息
     */
    @Operation(summary = "查询字典列表", description = "查询字典列表（下拉选项等场景）")
    @GetMapping("/dict")
    public List<LabelValueResult> dict(@Valid TenantQuery query) {
        return tenantService.dict(query);
    }
}