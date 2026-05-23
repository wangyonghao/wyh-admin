package top.wyhao.admin.system.otp.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wyhao.admin.system.otp.config.OtpProperties;
import top.wyhao.admin.system.otp.enums.OtpChannel;
import top.wyhao.admin.system.otp.enums.OtpScene;
import top.wyhao.admin.system.otp.util.TargetMasker;

import java.util.HashMap;
import java.util.Map;

/**
 * 模板服务
 *

 */
@Slf4j
@Service
public class TemplateService {

    private final OtpProperties otpProperties;
    private final TemplateEngine templateEngine;

    public TemplateService(OtpProperties otpProperties) {
        this.otpProperties = otpProperties;
        // 初始化模板引擎
        TemplateConfig config = new TemplateConfig();
        config.setResourceMode(TemplateConfig.ResourceMode.CLASSPATH);
        config.setPath(otpProperties.getTemplate().getBasePath());
        this.templateEngine = TemplateUtil.createEngine(config);
    }

    /**
     * 渲染模板
     *
     * @param channel   渠道
     * @param scene     场景
     * @param locale    语言
     * @param code      验证码
     * @param expiresIn 有效期（秒）
     * @param target    目标地址
     * @param ip        请求 IP
     * @return 渲染后的内容
     */
    public String render(OtpChannel channel, OtpScene scene, String locale,
                        String code, int expiresIn, String target, String ip) {
        // 加载模板
        String templateContent = loadTemplate(channel, scene, locale);

        // 构建数据模型
        Map<String, Object> model = new HashMap<>();
        model.put("code", code);
        model.put("expires_in", expiresIn / 60);
        model.put("target", TargetMasker.mask(target));
        model.put("timestamp", DateUtil.now());
        model.put("ip", ip);

        // 渲染模板
        Template template = templateEngine.getTemplate(templateContent);
        return template.render(model);
    }

    /**
     * 加载模板
     *
     * @param channel 渠道
     * @param scene   场景
     * @param locale  语言
     * @return 模板内容
     */
    private String loadTemplate(OtpChannel channel, OtpScene scene, String locale) {
        // 使用默认语言
        if (StrUtil.isBlank(locale)) {
            locale = otpProperties.getTemplate().getDefaultLocale();
        }

        // 构建模板路径
        String templatePath = String.format("%s/%s/%s_%s.txt",
            otpProperties.getTemplate().getBasePath(),
            channel.name().toLowerCase(),
            scene.name().toLowerCase(),
            locale);

        try {
            return ResourceUtil.readUtf8Str(templatePath);
        } catch (Exception e) {
            log.warn("模板文件不存在: {}, 尝试使用默认语言", templatePath);

            // 降级到默认语言
            String defaultPath = String.format("%s/%s/%s_%s.txt",
                otpProperties.getTemplate().getBasePath(),
                channel.name().toLowerCase(),
                scene.name().toLowerCase(),
                otpProperties.getTemplate().getDefaultLocale());

            try {
                return ResourceUtil.readUtf8Str(defaultPath);
            } catch (Exception ex) {
                log.error("默认模板文件也不存在: {}", defaultPath);
                // 返回通用模板
                return getDefaultTemplate(channel, scene);
            }
        }
    }

    /**
     * 获取通用模板
     */
    private String getDefaultTemplate(OtpChannel channel, OtpScene scene) {
        if (channel == OtpChannel.EMAIL) {
            return "【WYH Admin】验证码\n\n您的验证码为：{code}\n\n验证码有效期为 {expires_in} 分钟，请勿泄露给他人。\n\n如非本人操作，请忽略此邮件。";
        } else {
            return "【WYH Admin】您的验证码为：{code}，{expires_in}分钟内有效，请勿泄露。";
        }
    }
}
