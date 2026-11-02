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
import top.wyhao.system.cmn.sms.SmsConfig;
import top.wyhao.system.admin.app.dto.SmsConfigUpdateRequest;
import top.wyhao.settings.client.SmsConfigApi;

/**
 * 短信配置 API（路径保持与原 settings 一致）。
 */
@Tag(name = "系统配置 API")
@RestController
@RequiredArgsConstructor
public class SmsConfigController {

    private final SmsConfigApi smsConfigApi;

    @Operation(summary = "获取短信配置")
    @SaCheckPermission("system:config:get")
    @GetMapping("/system/config/sms")
    public SmsConfig getSmsConfig() {
        return smsConfigApi.getSmsConfig();
    }

    @Operation(summary = "更新短信配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/sms")
    public void updateSmsConfig(@RequestBody @Valid SmsConfigUpdateRequest config) {
        smsConfigApi.updateSmsConfig(config);
    }
}
