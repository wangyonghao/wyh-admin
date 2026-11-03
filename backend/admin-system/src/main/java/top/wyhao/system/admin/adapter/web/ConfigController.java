package top.wyhao.system.admin.adapter.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.text.CharSequenceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.system.cmn.mail.MailClient;
import top.wyhao.cmn.identity.LoginUser;
import top.wyhao.system.admin.adapter.client.MailAccounts;
import top.wyhao.settings.client.MailConfigVO;
import top.wyhao.identity.client.UserProfileApi;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.identity.client.UserProfile;
import top.wyhao.settings.client.SiteConfigApi;
import top.wyhao.settings.client.SiteConfigVO;
import top.wyhao.system.admin.domain.exception.ConfigException;
import top.wyhao.system.admin.app.dto.ConfigQuery;
import top.wyhao.system.admin.app.dto.config.RegisterConfigVO;
import top.wyhao.system.admin.app.dto.config.StorageConfigVO;
import top.wyhao.system.admin.app.dto.ConfigResult;
import top.wyhao.system.admin.app.service.ConfigService;

/**
 * 系统配置 API（settings 自有配置项 + 通用查询）。
 * <p>
 * 登录见 security；密码策略见 identity；短信见 notification。
 */
@Tag(name = "系统配置 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;
    private final SiteConfigApi siteConfigApi;
    private final MailClient mailService;
    private final UserProfileApi userProfileApi;

    @Operation(summary = "获取站点配置")
    @GetMapping("/system/config/site")
    public SiteConfigVO getSiteConfig() {
        return siteConfigApi.get();
    }

    @Operation(summary = "更新站点配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/site")
    public void updateSiteConfig(@RequestBody @Valid SiteConfigVO config) {
        siteConfigApi.update(config);
    }

    @Operation(summary = "获取注册配置")
    @GetMapping("/system/config/register")
    public RegisterConfigVO getRegisterConfig() {
        return configService.getRegisterConfig();
    }

    @Operation(summary = "更新注册配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/register")
    public void updateRegisterConfig(@RequestBody @Valid RegisterConfigVO config) {
        configService.updateRegisterConfig(config);
    }

    @Operation(summary = "获取邮件配置")
    @SaCheckPermission("system:config:get")
    @GetMapping("/system/config/mail")
    public MailConfigVO getMailConfig() {
        return configService.getMailConfig();
    }

    @Operation(summary = "更新邮件配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/mail")
    public void updateMailConfig(@RequestBody @Valid MailConfigVO config) {
        configService.updateMailConfig(config);
    }

    @Operation(summary = "发送测试邮件")
    @SaCheckPermission("system:config:edit")
    @PostMapping("/system/config/mail/test")
    public void sendTestMail(MailConfigVO mailConfig) {
        LoginUser loginUser = LoginUtil.getLoginUser();
        if (loginUser == null) {
            throw ConfigException.mailTestUserNotLoggedIn();
        }

        Long userId = LoginUtil.getUserId();
        UserProfile profile = userProfileApi.profile(userId);
        if (profile == null) {
            throw ConfigException.mailTestUserNotFound();
        }
        if (CharSequenceUtil.isBlank(profile.getEmail())) {
            throw ConfigException.mailTestUserEmailBlank();
        }

        String subject = "【系统测试】邮件配置测试";
        String content = String.format(
            """
                尊敬的 %s：
                
                这是一封测试邮件，用于验证系统邮件配置是否正确。
                
                如果您收到此邮件，说明邮件配置已成功！
                
                发送时间：%s
                
                此邮件由系统自动发送，请勿回复。""",
            profile.getUsername(),
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        try {
            mailService.sendTestMail(MailAccounts.from(mailConfig), profile.getEmail(), subject, content);
            log.info("测试邮件发送成功，收件人：{}", profile.getEmail());
        } catch (Exception e) {
            log.error("测试邮件发送失败", e);
            throw ConfigException.mailTestFailed(e.getMessage());
        }
    }

    @Operation(summary = "获取存储配置")
    @SaCheckPermission("system:config:get")
    @GetMapping("/system/config/storage")
    public StorageConfigVO getStorageConfig() {
        return configService.getStorageConfig();
    }

    @Operation(summary = "更新存储配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/storage")
    public void updateStorageConfig(@RequestBody @Valid StorageConfigVO config) {
        configService.updateStorageConfig(config);
    }

    @Operation(summary = "根据键查询配置")
    @SaCheckPermission("system:config:get")
    @GetMapping("/key/{configKey}")
    public ConfigResult getByKey(@PathVariable String configKey) {
        return configService.getByKey(configKey);
    }

    @Operation(summary = "导出")
    @SaCheckPermission("system:config:export")
    @GetMapping("/export")
    public void export(@Valid ConfigQuery query, HttpServletResponse response) {
        configService.export(query, response);
    }
}
