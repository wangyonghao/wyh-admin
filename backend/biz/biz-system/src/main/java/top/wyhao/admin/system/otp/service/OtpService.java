package top.wyhao.admin.system.otp.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.wyhao.admin.system.otp.config.OtpProperties;
import top.wyhao.admin.system.otp.enums.OtpChannel;
import top.wyhao.admin.system.otp.enums.OtpScene;
import top.wyhao.admin.system.otp.exception.OtpException;
import top.wyhao.admin.system.otp.model.OtpSession;
import top.wyhao.admin.system.otp.model.request.OtpSendRequest;
import top.wyhao.admin.system.otp.model.request.OtpVerifyRequest;
import top.wyhao.admin.system.otp.model.result.OtpSendResult;
import top.wyhao.admin.system.otp.model.result.OtpVerifyResult;
import top.wyhao.admin.system.otp.util.OtpCodeGenerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OTP 服务
 *
 * @author wyhao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String SESSION_KEY_PREFIX = "otp:session:";
    private static final String RATE_GLOBAL_KEY = "otp:rate:global";
    private static final String RATE_IP_KEY_PREFIX = "otp:rate:ip:";
    private static final String RATE_TARGET_KEY_PREFIX = "otp:rate:target:";
    private static final String FAIL_KEY_PREFIX = "otp:fail:";

    private final OtpProperties otpProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimiter rateLimiter;
    private final TemplateService templateService;
    private final List<ChannelService> channelServices;

    private Map<OtpChannel, ChannelService> channelServiceMap;

    /**
     * 发送验证码
     */
    public OtpSendResult send(OtpSendRequest req) {
        // 1. 参数校验
        validateSendRequest(req);

        // 2. 获取请求 IP
        String ip = getClientIp();

        // 3. 限流检查
        checkRateLimit(req.getChannel(), req.getTarget(), ip);

        // 4. 生成 UUID 和验证码
        String uuid = UUID.randomUUID().toString();
        String code = OtpCodeGenerator.generate(otpProperties.getCode().getLength());

        // 5. 渲染模板
        String content = templateService.render(
            req.getChannel(),
            req.getScene(),
            req.getLocale(),
            code,
            otpProperties.getCode().getExpiresIn(),
            req.getTarget(),
            ip
        );

        // 6. 发送验证码
        ChannelService channelService = getChannelService(req.getChannel());
        String subject = getSubject(req.getScene());
        channelService.send(req.getTarget(), subject, content);

        // 7. 存储会话数据
        long now = System.currentTimeMillis() / 1000;
        OtpSession session = OtpSession.builder()
            .uuid(uuid)
            .channel(req.getChannel())
            .scene(req.getScene())
            .target(req.getTarget())
            .code(code)
            .createdAt(now)
            .expiresAt(now + otpProperties.getCode().getExpiresIn())
            .verified(false)
            .failCount(0)
            .locale(req.getLocale())
            .build();

        String sessionKey = SESSION_KEY_PREFIX + uuid;
        stringRedisTemplate.opsForValue().set(
            sessionKey,
            JSONUtil.toJsonStr(session),
            otpProperties.getCode().getExpiresIn(),
            TimeUnit.SECONDS
        );

        // 8. 更新限流计数器
        updateRateLimitCounters(req.getChannel(), req.getTarget(), ip);

        // 9. 记录日志
        log.info("OTP 发送成功: uuid={}, channel={}, scene={}, target={}",
            uuid, req.getChannel(), req.getScene(), req.getTarget());

        return OtpSendResult.builder()
            .otpUuid(uuid)
            .expiresIn(otpProperties.getCode().getExpiresIn())
            .message("验证码已发送")
            .build();
    }

    /**
     * 验证验证码
     */
    public OtpVerifyResult verify(OtpVerifyRequest req) {
        // 1. 查询会话数据
        String sessionKey = SESSION_KEY_PREFIX + req.getOtpUuid();
        String sessionJson = stringRedisTemplate.opsForValue().get(sessionKey);

        if (StrUtil.isBlank(sessionJson)) {
            throw OtpException.notFound();
        }

        OtpSession session = JSONUtil.toBean(sessionJson, OtpSession.class);

        // 2. 检查是否已验证
        if (Boolean.TRUE.equals(session.getVerified())) {
            throw OtpException.alreadyUsed();
        }

        // 3. 检查是否过期
        long now = System.currentTimeMillis() / 1000;
        if (now > session.getExpiresAt()) {
            stringRedisTemplate.delete(sessionKey);
            throw OtpException.expired();
        }

        // 4. 检查失败次数
        String failKey = FAIL_KEY_PREFIX + req.getOtpUuid();
        long failCount = rateLimiter.getCount(failKey);
        if (failCount >= otpProperties.getRateLimit().getMaxFail()) {
            throw OtpException.locked();
        }

        // 5. 验证码比对
        if (!req.getCode().equals(session.getCode())) {
            // 失败计数 +1
            rateLimiter.increment(failKey, otpProperties.getCode().getExpiresIn());
            log.warn("OTP 验证失败: uuid={}, failCount={}", req.getOtpUuid(), failCount + 1);
            throw OtpException.invalid();
        }

        // 6. 标记为已验证
        session.setVerified(true);
        stringRedisTemplate.opsForValue().set(
            sessionKey,
            JSONUtil.toJsonStr(session),
            otpProperties.getCode().getExpiresIn(),
            TimeUnit.SECONDS
        );

        // 7. 删除失败计数
        rateLimiter.delete(failKey);

        log.info("OTP 验证成功: uuid={}", req.getOtpUuid());

        return OtpVerifyResult.builder()
            .verified(true)
            .message("验证成功")
            .build();
    }

    /**
     * 验证发送请求
     */
    private void validateSendRequest(OtpSendRequest req) {
        ChannelService channelService = getChannelService(req.getChannel());
        if (!channelService.validateTarget(req.getTarget())) {
            throw OtpException.invalidTarget("目标地址格式错误");
        }
    }

    /**
     * 检查限流
     */
    private void checkRateLimit(OtpChannel channel, String target, String ip) {
        OtpProperties.RateLimitConfig config = otpProperties.getRateLimit();

        // 全局限流
        if (!rateLimiter.isAllowed(RATE_GLOBAL_KEY, config.getGlobal().getMax(), config.getGlobal().getWindow())) {
            throw OtpException.rateLimitExceeded(config.getGlobal().getWindow());
        }

        // IP 限流
        String ipKey = RATE_IP_KEY_PREFIX + ip;
        if (!rateLimiter.isAllowed(ipKey, config.getIp().getMax(), config.getIp().getWindow())) {
            throw OtpException.rateLimitExceeded(config.getIp().getWindow());
        }

        // 目标地址限流
        String targetKey = RATE_TARGET_KEY_PREFIX + channel.name() + ":" + target;
        if (!rateLimiter.isAllowed(targetKey, config.getTarget().getMax(), config.getTarget().getWindow())) {
            throw OtpException.rateLimitExceeded(config.getTarget().getWindow());
        }
    }

    /**
     * 更新限流计数器
     */
    private void updateRateLimitCounters(OtpChannel channel, String target, String ip) {
        OtpProperties.RateLimitConfig config = otpProperties.getRateLimit();

        rateLimiter.increment(RATE_GLOBAL_KEY, config.getGlobal().getWindow());
        rateLimiter.increment(RATE_IP_KEY_PREFIX + ip, config.getIp().getWindow());
        rateLimiter.increment(RATE_TARGET_KEY_PREFIX + channel.name() + ":" + target, config.getTarget().getWindow());
    }

    /**
     * 获取渠道服务
     */
    private ChannelService getChannelService(OtpChannel channel) {
        if (channelServiceMap == null) {
            channelServiceMap = channelServices.stream()
                .collect(Collectors.toMap(ChannelService::getChannel, Function.identity()));
        }

        ChannelService service = channelServiceMap.get(channel);
        if (service == null) {
            throw new IllegalArgumentException("不支持的渠道: " + channel);
        }

        return service;
    }

    /**
     * 获取邮件主题
     */
    private String getSubject(OtpScene scene) {
        return scene.getDescription();
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
