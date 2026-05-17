
package top.wyhao.admin.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.wyhao.admin.cmn.mail.MailService;
import top.wyhao.admin.system.model.query.ConfigQuery;
import top.wyhao.admin.system.model.vo.ConfigResult;
import top.wyhao.admin.system.model.vo.config.*;
import top.wyhao.admin.system.service.ConfigService;
import top.wyhao.admin.system.service.UserService;
import top.wyhao.starter.core.UserContextHolder;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.model.MailConfig;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.web.core.model.SortQuery;

/**
 * 系统配置 API
 *
 * @author wyhao
 * @since 2024/04/26
 */
@Tag(name = "系统配置 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;
    private final MailService mailService;
    private final UserService userService;

    // ==================== 配置项专用接口 ====================

    /**
     * 获取站点配置
     */
    @Operation(summary = "获取站点配置")
    @GetMapping("/system/config/site")
    public SiteConfigVO getSiteConfig() {
        return configService.getSiteConfig();
    }

    /**
     * 更新站点配置
     */
    @Operation(summary = "更新站点配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/site")
    public void updateSiteConfig(@RequestBody @Valid SiteConfigVO config) {
        configService.updateSiteConfig(config);
    }

    /**
     * 获取登录配置
     */
    @Operation(summary = "获取登录配置")
    @GetMapping("/system/config/login")
    public LoginConfigVO getLoginConfig() {
        return configService.getLoginConfig();
    }

    /**
     * 更新登录配置
     */
    @Operation(summary = "更新登录配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/login")
    public void updateLoginConfig(@RequestBody @Valid LoginConfigVO config) {
        configService.updateLoginConfig(config);
    }

    /**
     * 获取注册配置
     */
    @Operation(summary = "获取注册配置")
    @GetMapping("/system/config/register")
    public RegisterConfigVO getRegisterConfig() {
        return configService.getRegisterConfig();
    }

    /**
     * 更新注册配置
     */
    @Operation(summary = "更新注册配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/register")
    public void updateRegisterConfig(@RequestBody @Valid RegisterConfigVO config) {
        configService.updateRegisterConfig(config);
    }

    /**
     * 获取邮件配置
     */
    @Operation(summary = "获取邮件配置")
    @SaCheckPermission("system:config:mail")
    @GetMapping("/system/config/mail")
    public MailConfig getMailConfig() {
        return configService.getMailConfig();
    }

    /**
     * 更新邮件配置
     */
    @Operation(summary = "更新邮件配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/mail")
    public void updateMailConfig(@RequestBody @Valid MailConfig config) {
        configService.updateMailConfig(config);
    }

    /**
     * 发送测试邮件
     */
    @Operation(summary = "发送测试邮件")
    @SaCheckPermission("system:config:edit")
    @PostMapping("/system/config/mail/test")
    public void sendTestMail(MailConfig mailConfig) {
        // 获取当前登录用户
        top.wyhao.starter.core.model.LoginUser loginUser = top.wyhao.starter.core.UserContextHolder.getCurrentUser();
        BizAssert.notNull(loginUser, "用户未登录");

        Long userId = UserContextHolder.getUserId();

        // 获取用户详细信息（包含邮箱）
        top.wyhao.admin.system.model.vo.user.UserDetailResult userDetail = userService.detail(userId);
        BizAssert.notNull(userDetail, "用户信息不存在");
        BizAssert.notBlank(userDetail.getEmail(), "用户邮箱为空，请先设置邮箱地址");

        // 发送测试邮件
        String subject = "【系统测试】邮件配置测试";
        String content = String.format(
                "尊敬的 %s：\n\n" +
                        "这是一封测试邮件，用于验证系统邮件配置是否正确。\n\n" +
                        "如果您收到此邮件，说明邮件配置已成功！\n\n" +
                        "发送时间：%s\n\n" +
                        "此邮件由系统自动发送，请勿回复。",
                userDetail.getUsername(),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        try {
            mailService.sendTestMail(mailConfig, userDetail.getEmail(), subject, content);
            log.info("测试邮件发送成功，收件人：{}", userDetail.getEmail());
        } catch (Exception e) {
            log.error("测试邮件发送失败", e);
            throw new BusinessException("测试邮件发送失败：" + e.getMessage());
        }

    }

    /**
     * 获取短信配置
     */
    @Operation(summary = "获取短信配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/system/config/sms")
    public SmsConfigVO getSmsConfig() {
        return configService.getSmsConfig();
    }

    /**
     * 更新短信配置
     */
    @Operation(summary = "更新短信配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/sms")
    public void updateSmsConfig(@RequestBody @Valid SmsConfigVO config) {
        configService.updateSmsConfig(config);
    }

    /**
     * 获取存储配置
     */
    @Operation(summary = "获取存储配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/system/config/storage")
    public StorageConfigVO getStorageConfig() {
        return configService.getStorageConfig();
    }

    /**
     * 更新存储配置
     */
    @Operation(summary = "更新存储配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/storage")
    public void updateStorageConfig(@RequestBody @Valid StorageConfigVO config) {
        configService.updateStorageConfig(config);
    }

    /**
     * 获取安全配置
     */
    @Operation(summary = "获取安全配置")
    @GetMapping("/system/config/security")
    public SecurityConfigVO getSecurityConfig() {
        return configService.getSecurityConfig();
    }

    /**
     * 更新安全配置
     */
    @Operation(summary = "更新安全配置")
    @SaCheckPermission("system:config:edit")
    @PutMapping("/system/config/security")
    public void updateSecurityConfig(@RequestBody @Valid SecurityConfigVO config) {
        configService.updateSecurityConfig(config);
    }

    // ==================== 通用 CRUD 接口 ====================

    /**
     * 根据键查询配置
     */
    @Operation(summary = "根据键查询配置")
    @SaCheckPermission("system:config:list")
    @GetMapping("/key/{configKey}")
    public ConfigResult getByKey(@PathVariable String configKey) {
        return configService.getByKey(configKey);
    }

    /**
     * 导出
     */
    @Operation(summary = "导出")
    @SaCheckPermission("system:config:export")
    @GetMapping("/export")
    public void export(@Valid ConfigQuery query, @Valid SortQuery sortQuery, HttpServletResponse response) {
        configService.export(query, sortQuery, response);
    }
}
