package top.wyhao.admin.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.system.model.bo.MenuRequest;
import top.wyhao.admin.system.model.query.MenuQuery;
import top.wyhao.admin.system.model.vo.MenuTreeVO;
import top.wyhao.admin.system.model.vo.MenuVO;
import top.wyhao.admin.system.service.MenuService;
import top.wyhao.starter.web.core.model.SortQuery;
import top.wyhao.starter.web.core.model.IdResult;

import java.util.List;

/**
 * 菜单管理 API
 *

 * @since 2023/2/15 20:21
 */
@Tag(name = "菜单管理 API")
@RestController
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    /**
     * 查询树列表
     *
     * @param query 查询条件
     * @return 树列表信息
     */
    @Operation(summary = "查询树列表", description = "查询树列表")
    @GetMapping("/system/menu")
    public List<MenuTreeVO> tree(@Valid MenuQuery query) {
        return menuService.tree(query);
    }

    /**
     * 查询菜单详情
     *
     * @param id ID
     * @return 详情信息
     */
    @Operation(summary = "查询菜单详情", description = "查询菜单详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/system/menu/{id}")
    public MenuVO get(@PathVariable Long id) {
        return menuService.get(id);
    }

    /**
     * 创建菜单
     *
     * @param req 创建菜单请求
     * @return ID
     */
    @Operation(summary = "创建菜单", description = "创建菜单")
    @PostMapping("/system/menu")
    public IdResult<Long> create(@RequestBody @Valid MenuRequest req) {
        return new IdResult<>(menuService.create(req));
    }

    /**
     * 修改菜单
     *
     * @param req 修改请求参数
     * @param id  ID
     */
    @Operation(summary = "修改菜单", description = "修改菜单")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @PutMapping("/system/menu/{id}")
    public void update(@RequestBody @Valid MenuRequest req, @PathVariable Long id) {
        menuService.update(id, req);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @DeleteMapping("/system/menu/{id}")
    public void delete(@PathVariable Long id) {
        menuService.delete(id);
    }

    /**
     * 导出
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @param response  响应对象
     */
    @Operation(summary = "导出数据", description = "导出数据")
    @GetMapping("/system/menu/export")
    public void export(@Valid MenuQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        menuService.export(query, sortQuery, response);
    }

    /**
     * 查询树型字典列表
     *
     * @param query     查询条件
     * @param sortQuery 排序查询条件
     * @return 树型字典列表信息
     */
    @Operation(summary = "查询树型字典列表", description = "查询树型结构字典列表（树型结构下拉选项等场景）")
    @GetMapping("/system/menu/dict/tree")
    public List<MenuTreeVO> treeDict(@Valid MenuQuery query, @Valid SortQuery sortQuery) {
        return menuService.tree(query);
    }
}