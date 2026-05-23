package top.wyhao.admin.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.wf.captcha.base.Captcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.admin.auth.model.CaptchaImageResult;
import top.wyhao.admin.cmn.mail.MailClient;
import top.wyhao.admin.cmn.sms.SmsClient;
import top.wyhao.admin.config.CaptchaProperties;
import top.wyhao.admin.system.entity.SysSmsConfig;
import top.wyhao.admin.system.model.vo.config.LoginConfigVO;
import top.wyhao.admin.system.model.vo.config.SiteConfigVO;
import top.wyhao.admin.system.service.ConfigService;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.captcha.graphic.core.GraphicCaptchaService;
import top.wyhao.starter.core.autoconfigure.application.ApplicationProperties;
import top.wyhao.starter.core.exception.BusinessException;
import top.wyhao.starter.core.model.Result;
import top.wyhao.starter.core.util.TemplateUtils;
import top.wyhao.starter.core.validation.Mobile;
import top.wyhao.starter.web.ratelimit.LimitType;
import top.wyhao.starter.web.ratelimit.RateLimiter;
import top.wyhao.starter.web.ratelimit.RateLimiters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 人机检测 API
 * <p>解决“自动化攻击/防机器滥用”问题</p>
 */
@Tag(name = "人机检测 API")
@SaIgnore
@Validated
@RestController
@RequiredArgsConstructor
public class CaptchaController {
    private static final String CAPTCHA_KEY = "login:captcha:";
    private final ApplicationProperties applicationProperties;
    private final CaptchaProperties captchaProperties;
    private final GraphicCaptchaService graphicCaptchaService;
    private final ConfigService configService;

    private final MailClient mailClient;
    private final SmsClient smsClient;


    @Operation(summary = "获取图片验证码", description = "获取图片验证码（Base64编码，带图片格式：data:image/gif;base64）")
    @GetMapping("/captcha/image")
    public CaptchaImageResult getImageCaptcha() {
        LoginConfigVO loginConfigVO = configService.getLoginConfig();
        boolean loginCaptchaEnabled = loginConfigVO.getCaptchaEnabled();
        if (!loginCaptchaEnabled) {
            return CaptchaImageResult.builder().isEnabled(false).build();
        }
        Captcha captcha = graphicCaptchaService.createCaptchaImage();
        long expireTime = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now().plusSeconds(captchaProperties.getExpirationInSeconds()));

        CaptchaImageResult vo = CaptchaImageResult.builder()
                .uuid(IdUtil.fastUUID())
                .img(captcha.toBase64())
                .expireTime(expireTime)
                .isEnabled(true)
                .build();

        RedisUtils.set(CAPTCHA_KEY + vo.getUuid(), captcha.text(), Duration.ofSeconds(captchaProperties.getExpirationInSeconds()));
        return vo;
    }

    /**
     * 获取邮箱验证码
     *
     * <p>
     * 限流规则：<br>
     * 1.同一邮箱同一模板，1分钟2条，1小时8条，24小时20条 <br>
     * 2.同一邮箱所有模板 24 小时 100 条 <br>
     * 3.同一 IP 每分钟限制发送 30 条
     * </p>
     *
     * @param email      邮箱
     * @return {@link Result }
     */
    @Operation(summary = "获取邮箱验证码", description = "发送验证码到指定邮箱")
    @GetMapping("/captcha/email")
    @RateLimiters({
            @RateLimiter(name = CAPTCHA_KEY + "MIN", key = "#email + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.email.templatePath')", rate = 2, interval = 1, unit = TimeUnit.MINUTES, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY + "HOUR", key = "#email + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.email.templatePath')", rate = 8, interval = 1, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY + "DAY'", key = "#email + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.email.templatePath')", rate = 20, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY, key = "#email", rate = 100, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY, key = "#email", rate = 30, interval = 1, unit = TimeUnit.MINUTES, type = LimitType.IP, message = "获取验证码操作太频繁，请稍后再试")})
    public String getEmailCaptcha(@NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email) {
        // 生成验证码
        CaptchaProperties.CaptchaEmail captchaEmail = captchaProperties.getEmail();
        String captcha = RandomUtil.randomNumbers(captchaEmail.getLength());
        Long expirationInMinutes = captchaEmail.getExpirationInMinutes();
        // 发送验证码
        SiteConfigVO site = configService.getSiteConfig();
        String content = TemplateUtils.render(captchaEmail.getTemplatePath(), Dict.create()
                .set("siteUrl", applicationProperties.getUrl())
                .set("siteTitle", site.getSiteName())
                .set("siteCopyright", site.getSiteCopyright())
                .set("captcha", captcha)
                .set("expiration", expirationInMinutes));
        mailClient.sendHtml(email, "【%s】邮箱验证码".formatted(applicationProperties.getName()), content);
        // 缓存验证码
        String captchaKey = CAPTCHA_KEY + email;
        RedisUtils.set(captchaKey, captcha, Duration.ofMinutes(expirationInMinutes));
        return "发送成功，验证码有效期 %s 分钟".formatted(expirationInMinutes);
    }

    /**
     * 获取短信验证码
     *
     * <p>
     * 限流规则：<br>
     * 1.同一号码同一模板，1分钟2条，1小时8条，24小时20条 <br>
     * 2.同一号码所有模板 24 小时 100 条 <br>
     * 3.同一 IP 每分钟限制发送 30 条
     * </p>
     *
     * @param phone      手机号
     * @return {@link Result }
     */
    @Operation(summary = "获取短信验证码", description = "发送验证码到指定手机号")
    @GetMapping("/captcha/sms")
    public String getSmsCaptcha(@NotBlank(message = "手机号不能为空") @Mobile String phone) {
        // 行为验证码校验

        CaptchaProperties.CaptchaSms captchaSms = captchaProperties.getSms();
        // 生成验证码
        String captcha = RandomUtil.randomNumbers(captchaSms.getLength());
        Long expirationInMinutes = captchaSms.getExpirationInMinutes();
        // 发送验证码

        Map<String,String> valueMap =  Map.of(captchaSms.getCodeKey(), captcha, captchaSms.getTimeKey(), String.valueOf(expirationInMinutes));
        String templateId = "";
        boolean isSuccess = smsClient.send(phone, templateId, (LinkedHashMap<String, String>)valueMap);
        if(!isSuccess){
            throw new BusinessException("验证码发送失败");
        }
        // 保存验证码
        String captchaKey = CAPTCHA_KEY + phone;
        RedisUtils.set(captchaKey, captcha, Duration.ofMinutes(expirationInMinutes));
        return "发送成功，验证码有效期 %s 分钟".formatted(expirationInMinutes);
    }
}
