package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.system.admin.client.PasswordPolicyConfig;
import top.wyhao.system.admin.client.PasswordPolicyConfigApi;

/**
 * 密码策略配置 API（路径保持 /system/config/security）。
 */
@Tag(name = "系统配置 API")
@RestController
@RequiredArgsConstructor
public class PasswordPolicyConfigController {

    private final PasswordPolicyConfigApi passwordPolicyConfigApi;

    @Operation(summary = "获取安全配置")
    @GetMapping("/system/config/security")
    public PasswordPolicyConfig getSecurityConfig() {
        return passwordPolicyConfigApi.get();
    }

    @Operation(summary = "更新安全配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/security")
    public void updateSecurityConfig(@RequestBody @Valid PasswordPolicyConfig config) {
        passwordPolicyConfigApi.update(config);
    }
}
