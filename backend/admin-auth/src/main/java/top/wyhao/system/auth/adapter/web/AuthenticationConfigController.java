package top.wyhao.system.auth.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.system.auth.adapter.client.LoginConfigApi;
import top.wyhao.system.auth.adapter.client.config.LoginConfigVO;

/**
 * 登录配置 API（路径保持不变）。
 */
@Tag(name = "系统配置 API")
@RestController
@RequiredArgsConstructor
public class AuthenticationConfigController {

    private final LoginConfigApi loginConfigApi;

    @Operation(summary = "获取登录配置")
    @GetMapping("/system/config/login")
    public LoginConfigVO getAuthenticationConfig() {
        return loginConfigApi.get();
    }

    @Operation(summary = "更新登录配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/login")
    public void updateAuthenticationConfig(@RequestBody @Valid LoginConfigVO config) {
        loginConfigApi.update(config);
    }
}
