package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.mybatis.query.PageResult;
import top.wyhao.system.admin.app.dto.OnlineUserResult;
import top.wyhao.system.admin.app.service.OnlineSessionService;
import top.wyhao.web.core.model.PageQuery;

import java.util.List;

/**
 * 在线会话 API（监控管理）
 */
@Tag(name = "在线会话 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/online")
public class OnlineSessionController {

    private final OnlineSessionService onlineSessionService;

    @Operation(summary = "分页查询在线用户", description = "按用户名/IP/地点关键字过滤，返回真实 total")
    @SaCheckPermission("monitor:online:list")
    @GetMapping
    public PageResult<OnlineUserResult> page(
            @RequestParam(required = false) String keyword,
            @Valid PageQuery pageQuery) {
        return onlineSessionService.page(keyword, pageQuery);
    }

    @Operation(summary = "强退在线用户", description = "按 Token 强退在线用户")
    @Parameter(name = "token", description = "令牌", example = "ey****J9.ey****fQ.7q****vE", in = ParameterIn.PATH)
    @SaCheckPermission("monitor:online:kickout")
    @DeleteMapping("/{token}")
    public void revoke(@PathVariable String token) {
        onlineSessionService.revoke(token);
    }

    @Operation(summary = "批量强退在线用户", description = "按 Token 列表批量强退（自动跳过当前会话）")
    @SaCheckPermission("monitor:online:kickout")
    @DeleteMapping
    public void revokeBatch(@RequestBody List<String> tokens) {
        onlineSessionService.revokeBatch(tokens);
    }
}
