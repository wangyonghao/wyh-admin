package top.wyhao.admin.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.wf.captcha.base.Captcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.wyhao.admin.auth.model.CaptchaImageResult;
import top.wyhao.admin.config.CaptchaProperties;
import top.wyhao.admin.system.entity.SysSmsConfig;
import top.wyhao.admin.system.model.vo.config.LoginConfigVO;
import top.wyhao.admin.system.model.vo.config.SiteConfigVO;
import top.wyhao.admin.system.service.ConfigService;
import top.wyhao.admin.system.service.SmsConfigService;
import top.wyhao.starter.cache.redisson.util.RedisUtils;
import top.wyhao.starter.captcha.graphic.core.GraphicCaptchaService;
import top.wyhao.starter.core.autoconfigure.application.ApplicationProperties;
import top.wyhao.starter.core.model.R;
import top.wyhao.starter.core.util.TemplateUtils;
import top.wyhao.starter.core.util.validation.BizAssert;
import top.wyhao.starter.core.util.validation.ValidationUtils;
import top.wyhao.starter.core.validation.Mobile;
import top.wyhao.admin.cmn.mail.MailService;
import top.wyhao.starter.web.ratelimit.LimitType;
import top.wyhao.starter.web.ratelimit.RateLimiter;
import top.wyhao.starter.web.ratelimit.RateLimiters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码 API
 * <p>解决“自动化攻击/防机器滥用”问题</p>
 */
@Tag(name = "验证码 API")
@SaIgnore
@Validated
@RestController
@RequiredArgsConstructor
public class CaptchaController {
    private static final String CAPTCHA_KEY = "login:captcha:";
    private final ApplicationProperties applicationProperties;
    private final CaptchaProperties captchaProperties;
    private final GraphicCaptchaService graphicCaptchaService;
    private final SmsConfigService smsConfigService;
    private final ConfigService configService;

    private final MailService mailService;

    @Operation(summary = "获取行为验证码", description = "获取行为验证码（Base64编码）")
    @GetMapping("/captcha/behavior")
    public Object getBehaviorCaptcha(CaptchaVO captchaReq, HttpServletRequest request) {
        CaptchaService behaviorCaptchaService = SpringUtil.getBean(CaptchaService.class);
        captchaReq.setBrowserInfo(JakartaServletUtil.getClientIP(request) + request.getHeader(HttpHeaders.USER_AGENT));
        ResponseModel responseModel = behaviorCaptchaService.get(captchaReq);
        BizAssert.isTrue(() -> !CharSequenceUtil.equals(RepCodeEnum.SUCCESS.getCode(), responseModel
                .getRepCode()), responseModel.getRepMsg());
        return responseModel.getRepData();
    }

    @Operation(summary = "校验行为验证码", description = "校验行为验证码")
    @PostMapping("/captcha/behavior")
    public Object checkBehaviorCaptcha(@RequestBody CaptchaVO captchaReq, HttpServletRequest request) {
        CaptchaService behaviorCaptchaService = SpringUtil.getBean(CaptchaService.class);
        captchaReq.setBrowserInfo(JakartaServletUtil.getClientIP(request) + request.getHeader(HttpHeaders.USER_AGENT));
        return behaviorCaptchaService.check(captchaReq);
    }

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
     * @param captchaReq 行为验证码请求参数
     * @return {@link R }
     */
    @Operation(summary = "获取邮箱验证码", description = "发送验证码到指定邮箱")
    @GetMapping("/captcha/email")
    @RateLimiters({
            @RateLimiter(name = CAPTCHA_KEY + "MIN", key = "#email + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.email.templatePath')", rate = 2, interval = 1, unit = TimeUnit.MINUTES, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY + "HOUR", key = "#email + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.email.templatePath')", rate = 8, interval = 1, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY + "DAY'", key = "#email + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.email.templatePath')", rate = 20, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY, key = "#email", rate = 100, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY, key = "#email", rate = 30, interval = 1, unit = TimeUnit.MINUTES, type = LimitType.IP, message = "获取验证码操作太频繁，请稍后再试")})
    public String getEmailCaptcha(@NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
                                 CaptchaVO captchaReq) throws MessagingException {
        // 行为验证码校验
        CaptchaService behaviorCaptchaService = SpringUtil.getBean(CaptchaService.class);
        ResponseModel verificationRes = behaviorCaptchaService.verification(captchaReq);
        ValidationUtils.throwIfNotEqual(verificationRes.getRepCode(), RepCodeEnum.SUCCESS.getCode(), verificationRes
                .getRepMsg());
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
        mailService.sendHtml(email, "【%s】邮箱验证码".formatted(applicationProperties.getName()), content);
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
     * @param captchaReq 行为验证码请求参数
     * @return {@link R }
     */
    @Operation(summary = "获取短信验证码", description = "发送验证码到指定手机号")
    @GetMapping("/captcha/sms")
    @RateLimiters({
            @RateLimiter(name = CAPTCHA_KEY + "MIN", key = "#phone + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.sms.templateId')", rate = 2, interval = 1, unit = TimeUnit.MINUTES, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY + "HOUR", key = "#phone + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.sms.templateId')", rate = 8, interval = 1, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY + "DAY'", key = "#phone + ':' + T(cn.hutool.extra.spring.SpringUtil).getProperty('captcha.sms.templateId')", rate = 20, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY, key = "#phone", rate = 100, interval = 24, unit = TimeUnit.HOURS, message = "获取验证码操作太频繁，请稍后再试"),
            @RateLimiter(name = CAPTCHA_KEY, key = "#phone", rate = 30, interval = 1, unit = TimeUnit.MINUTES, type = LimitType.IP, message = "获取验证码操作太频繁，请稍后再试")})
    public String getSmsCaptcha(@NotBlank(message = "手机号不能为空") @Mobile String phone, CaptchaVO captchaReq) {
        // 行为验证码校验
        CaptchaService behaviorCaptchaService = SpringUtil.getBean(CaptchaService.class);
        ResponseModel verificationRes = behaviorCaptchaService.verification(captchaReq);
        ValidationUtils.throwIfNotEqual(verificationRes.getRepCode(), RepCodeEnum.SUCCESS.getCode(), verificationRes
                .getRepMsg());
        CaptchaProperties.CaptchaSms captchaSms = captchaProperties.getSms();
        // 生成验证码
        String captcha = RandomUtil.randomNumbers(captchaSms.getLength());
        Long expirationInMinutes = captchaSms.getExpirationInMinutes();
        // 获取短信配置
        SysSmsConfig smsConfig = smsConfigService.getDefaultConfig();
        SmsBlend smsBlend = smsConfig != null
                ? SmsFactory.getBySupplier(smsConfig.getSupplier())
                : SmsFactory.getSmsBlend();
        Map<String, String> messageMap = MapUtil.newHashMap(2, true);
        messageMap.put(captchaSms.getCodeKey(), captcha);
        messageMap.put(captchaSms.getTimeKey(), String.valueOf(expirationInMinutes));
        // 发送验证码
        SmsResponse smsResponse = smsBlend.sendMessage(phone, (LinkedHashMap<String, String>) messageMap);
        BizAssert.isTrue(!smsResponse.isSuccess(), "验证码发送失败");
        // 保存验证码
        String captchaKey = CAPTCHA_KEY + phone;
        RedisUtils.set(captchaKey, captcha, Duration.ofMinutes(expirationInMinutes));
        return "发送成功，验证码有效期 %s 分钟".formatted(expirationInMinutes);
    }
}
