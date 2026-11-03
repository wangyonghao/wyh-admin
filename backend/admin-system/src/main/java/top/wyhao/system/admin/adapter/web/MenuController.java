package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wyhao.system.admin.app.dto.MenuTreeVO;
import top.wyhao.system.admin.app.dto.MenuVO;
import top.wyhao.system.admin.app.service.MenuService;
import top.wyhao.web.core.model.IdResult;

import java.util.List;
import top.wyhao.system.admin.app.dto.MenuQuery;
import top.wyhao.system.admin.app.dto.MenuRequest;
import top.wyhao.system.admin.app.dto.MenuSortRequest;

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

    @Operation(summary = "查询全部菜单", description = "管理页使用，包含停用项和按钮")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/system/menu/all")
    public List<MenuVO> all() {
        return menuService.listAll();
    }

    @Operation(summary = "保存菜单顺序", description = "按当前同级顺序写入上级和排序")
    @SaCheckPermission("system:menu:update")
    @PutMapping("/system/menu/sort")
    public void sort(@RequestBody @Valid MenuSortRequest req) {
        menuService.sort(req);
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
    @SaCheckPermission("system:menu:create")
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
    @SaCheckPermission("system:menu:update")
    @PutMapping("/system/menu/{id}")
    public void update(@RequestBody @Validated(MenuRequest.Update.class) MenuRequest req, @PathVariable Long id) {
        menuService.update(id, req);
    }

    /**
     * 删除
     *
     * @param id ID
     */
    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @SaCheckPermission("system:menu:delete")
    @DeleteMapping("/system/menu/{id}")
    public void delete(@PathVariable Long id) {
        menuService.delete(id);
    }

    /**
     * 导出
     *
     * @param query    查询条件
     * @param response 响应对象
     */
    @Operation(summary = "导出数据", description = "导出数据")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/system/menu/export")
    public void export(@Valid MenuQuery query, HttpServletResponse response) {
        menuService.export(query, response);
    }

    /**
     * 查询树型字典列表
     *
     * @param query     查询条件
     * @return 树型字典列表信息
     */
    @Operation(summary = "查询树型字典列表", description = "查询树型结构字典列表（树型结构下拉选项等场景）")
    @GetMapping("/system/menu/dict/tree")
    public List<MenuTreeVO> treeDict(@Valid MenuQuery query) {
        return menuService.tree(query);
    }
}