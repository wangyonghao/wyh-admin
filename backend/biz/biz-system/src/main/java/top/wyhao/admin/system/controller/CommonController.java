package top.wyhao.admin.system.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.alicp.jetcache.anno.Cached;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.admin.system.service.DictService;
import top.wyhao.admin.system.service.FileService;
import top.wyhao.starter.core.constant.CacheConstants;
import top.wyhao.starter.tenant.context.TenantContextHolder;
import top.wyhao.starter.web.core.model.LabelValueResult;

import java.util.List;

/**
 * 公共 API
 *

 * @since 2023/1/22 21:48
 */
@Tag(name = "公共 API")
@Validated
@RestController("systemCommonController")
@RequiredArgsConstructor
@RequestMapping("/system/common")
public class CommonController {

    private final FileService fileService;
    private final DictService dictService;


    @Operation(summary = "查询字典", description = "查询字典列表")
    @Parameter(name = "dictType", description = "字典类型", example = "notice_type", in = ParameterIn.PATH)
    @GetMapping("/dict/{dictType}")
    public List<LabelValueResult<String>> listDict(@PathVariable String dictType) {
        return dictService.listByDictType(dictType);
    }

    @SaIgnore
    @Operation(summary = "查询租户开启状态", description = "查询租户开启状态")
    @GetMapping("/dict/option/tenant")
    @Cached(key = "'TENANT'", name = CacheConstants.CONFIG_KEY_PREFIX)
    public Boolean tenantEnabled() {
        return TenantContextHolder.isTenantEnabled();
    }
}