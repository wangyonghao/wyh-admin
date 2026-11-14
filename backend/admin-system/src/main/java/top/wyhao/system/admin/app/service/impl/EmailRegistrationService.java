package top.wyhao.system.admin.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import top.wyhao.identity.client.EffectivePasswordRules;
import top.wyhao.identity.client.EmailRegistrationConfirm;
import top.wyhao.identity.client.EmailRegistrationStart;
import top.wyhao.identity.client.EmailRegistrationTicket;
import top.wyhao.identity.client.PasswordPolicyConfigApi;
import top.wyhao.system.admin.app.service.RegistrationTransactions;
import top.wyhao.system.admin.app.service.TimeSource;
import top.wyhao.system.admin.app.service.UserProfileService;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.EmailRegistrationRepository;
import top.wyhao.system.admin.domain.gateway.PasswordPolicyGateway;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.EmailRegistration;
import top.wyhao.system.admin.domain.model.PasswordHistory;
import top.wyhao.system.admin.domain.model.PasswordPolicy;
import top.wyhao.system.admin.domain.service.RegisterUsernameAllocator;
import top.wyhao.system.admin.domain.service.RegistrationToken;
import top.wyhao.system.admin.domain.service.UserDomainService;
import top.wyhao.system.cmn.mail.MailClient;
import top.wyhao.tenant.context.TenantContext;
import top.wyhao.tenant.context.TenantContextHolder;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 邮箱注册：先发一次性链接，确认后才创建可登录账号。
 */
@Service
@RequiredArgsConstructor
public class EmailRegistrationService {

    static final int MIN_TTL_MINUTES = 5;
    static final int MAX_TTL_MINUTES = 1440;
    static final int MIN_RESEND_SECONDS = 30;
    static final int MAX_RESEND_SECONDS = 3600;

    private final EmailRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final UserProfileService userProfileService;
    private final UserDomainService userDomainService;
    private final PasswordPolicyGateway passwordPolicyGateway;
    private final PasswordPolicyConfigApi passwordPolicyConfigApi;
    private final PasswordEncoder passwordEncoder;
    private final MailClient mailClient;
    private final RegistrationTransactions transactions;
    private final TimeSource timeSource;

    public EmailRegistrationTicket begin(EmailRegistrationStart start) {
        String email = normalizeEmail(start.email());
        String publicBaseUrl = requirePublicBaseUrl(start.publicBaseUrl());
        int ttlMinutes = clamp(start.ttlMinutes(), MIN_TTL_MINUTES, MAX_TTL_MINUTES);
        int resendSeconds = clamp(start.resendIntervalSeconds(), MIN_RESEND_SECONDS, MAX_RESEND_SECONDS);
        assertPassword(start.rawPassword());
        if (userRepository.existsEmail(email, null)) {
            throw UserException.emailExists();
        }
        Long tenantId = currentTenantId();
        transactions.run(() -> registrationRepository.deleteStale(timeSource.now()));

        try {
            return saveAndSend(email, start.rawPassword(), start.departmentId(), start.roleId(),
                    tenantId, publicBaseUrl, ttlMinutes, resendSeconds, true);
        } catch (DataIntegrityViolationException ex) {
            return saveAndSend(email, start.rawPassword(), start.departmentId(), start.roleId(),
                    tenantId, publicBaseUrl, ttlMinutes, resendSeconds, true);
        }
    }

    public EmailRegistrationTicket resend(String email, String publicBaseUrl, int ttlMinutes, int resendIntervalSeconds) {
        String normalized = normalizeEmail(email);
        String baseUrl = requirePublicBaseUrl(publicBaseUrl);
        int ttl = clamp(ttlMinutes, MIN_TTL_MINUTES, MAX_TTL_MINUTES);
        int interval = clamp(resendIntervalSeconds, MIN_RESEND_SECONDS, MAX_RESEND_SECONDS);
        Long tenantId = currentTenantId();
        transactions.run(() -> registrationRepository.deleteStale(timeSource.now()));
        EmailRegistration open = registrationRepository.findOpen(tenantId, normalized);
        if (open == null) {
            throw UserException.of("REGISTER_NOTHING_TO_RESEND", "没有可重发的验证邮件");
        }
        return saveAndSend(normalized, null, open.getDepartmentId(), open.getRoleId(),
                tenantId, baseUrl, ttl, interval, false);
    }

    public EmailRegistrationConfirm confirm(String token, boolean registrationEnabled) {
        if (!registrationEnabled) {
            throw UserException.of("REGISTRATION_DISABLED", "系统未开放注册");
        }
        String tokenHash = RegistrationToken.hash(token);
        if (tokenHash.isEmpty()) {
            throw UserException.of("REGISTER_LINK_INVALID", "链接无效或已过期");
        }
        return transactions.call(() -> confirmLocked(tokenHash));
    }

    private EmailRegistrationConfirm confirmLocked(String tokenHash) {
        registrationRepository.deleteStale(timeSource.now());
        EmailRegistration row = registrationRepository.lockByTokenHash(tokenHash);
        if (row == null) {
            throw UserException.of("REGISTER_LINK_INVALID", "链接无效或已过期");
        }
        if (row.consumed()) {
            return new EmailRegistrationConfirm(EmailRegistrationConfirm.ALREADY_VERIFIED);
        }
        if (row.expired(timeSource.now())) {
            throw UserException.of("REGISTER_LINK_INVALID", "链接无效或已过期");
        }
        if (userRepository.existsEmail(row.getEmail(), null)) {
            LocalDateTime now = timeSource.now();
            row.setConsumedAt(now);
            row.setUpdateTime(now);
            registrationRepository.update(row);
            return new EmailRegistrationConfirm(EmailRegistrationConfirm.ALREADY_VERIFIED);
        }
        String username = row.getUsername();
        if (userRepository.existsUsername(username)) {
            username = RegisterUsernameAllocator.secure().allocate(userRepository::existsUsername, "", false);
            row.setUsername(username);
        }
        createInTenant(row);
        LocalDateTime now = timeSource.now();
        row.setConsumedAt(now);
        row.setUpdateTime(now);
        registrationRepository.update(row);
        return new EmailRegistrationConfirm(EmailRegistrationConfirm.VERIFIED);
    }

    private void createInTenant(EmailRegistration row) {
        TenantContext previous = TenantContextHolder.getContext();
        boolean previousIgnore = TenantContextHolder.isIgnore();
        try {
            TenantContext forced = new TenantContext();
            forced.setTenantId(row.getTenantId());
            TenantContextHolder.setContext(forced);
            TenantContextHolder.setIgnore(false);
            userProfileService.createVerifiedEmailAccount(
                    row.getUsername(),
                    row.getEmail(),
                    row.getPasswordHash(),
                    row.getDepartmentId(),
                    row.getRoleId());
        } finally {
            TenantContextHolder.setContext(previous);
            TenantContextHolder.setIgnore(previousIgnore);
        }
    }

    private EmailRegistrationTicket saveAndSend(String email, String rawPassword, Long departmentId, Long roleId,
                                                 Long tenantId, String publicBaseUrl, int ttlMinutes,
                                                 int resendSeconds, boolean replacePassword) {
        Prepared prepared = transactions.call(() -> prepare(
                email, rawPassword, departmentId, roleId, tenantId, ttlMinutes, resendSeconds, replacePassword));
        if (prepared.cooldown()) {
            return new EmailRegistrationTicket(
                    EmailRegistrationTicket.COOLDOWN, false, prepared.resendAfterSeconds(), ttlMinutes);
        }
        boolean delivered = false;
        try {
            mailClient.sendText(email, "完成注册", mailBody(publicBaseUrl, prepared.token(), ttlMinutes));
            delivered = true;
        } catch (RuntimeException ex) {
            LocalDateTime failedAt = timeSource.now();
            transactions.run(() -> registrationRepository.markSend(
                    prepared.rowId(), EmailRegistration.SEND_FAILED, null, failedAt));
            throw UserException.of("REGISTER_MAIL_UNSENT", "验证邮件没有发出，请稍后重试");
        }
        LocalDateTime sentAt = timeSource.now();
        transactions.run(() -> registrationRepository.markSend(
                prepared.rowId(), EmailRegistration.SEND_SENT, sentAt, sentAt));
        return new EmailRegistrationTicket(EmailRegistrationTicket.PENDING, delivered, resendSeconds, ttlMinutes);
    }

    private Prepared prepare(String email, String rawPassword, Long departmentId, Long roleId, Long tenantId,
                             int ttlMinutes, int resendSeconds, boolean replacePassword) {
        LocalDateTime now = timeSource.now();
        EmailRegistration open = registrationRepository.findOpen(tenantId, email);
        if (open != null && EmailRegistration.SEND_SENT.equals(open.getSendStatus()) && open.getSentAt() != null) {
            long wait = Duration.between(now, open.getSentAt().plusSeconds(resendSeconds)).getSeconds();
            if (wait > 0) {
                return Prepared.cooldown((int) wait);
            }
        }
        PasswordPolicy policy = loginCompatiblePolicy(passwordPolicyGateway.current());
        String username = open != null && !userRepository.existsUsername(open.getUsername())
                ? open.getUsername()
                : RegisterUsernameAllocator.secure().allocate(
                userRepository::existsUsername,
                rawPassword == null ? "" : rawPassword,
                rawPassword != null && !policy.allowContainUsername());
        if (replacePassword) {
            userDomainService.assertCompliant(rawPassword, policy, username, PasswordHistory.empty());
        }
        String token = RegistrationToken.newToken();
        LocalDateTime expiresAt = now.plusMinutes(ttlMinutes);
        if (open == null) {
            EmailRegistration created = new EmailRegistration();
            created.setTenantId(tenantId);
            created.setEmail(email);
            created.setUsername(username);
            created.setPasswordHash(passwordEncoder.encode(rawPassword));
            created.setTokenHash(RegistrationToken.hash(token));
            created.setDepartmentId(departmentId);
            created.setRoleId(roleId);
            created.setExpiresAt(expiresAt);
            created.setSendStatus(EmailRegistration.SEND_PENDING);
            created.setCreateTime(now);
            created.setUpdateTime(now);
            registrationRepository.insert(created);
            return Prepared.ready(created.getId(), token);
        }
        open.setUsername(username);
        if (replacePassword) {
            open.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        open.setTokenHash(RegistrationToken.hash(token));
        open.setExpiresAt(expiresAt);
        open.setSendStatus(EmailRegistration.SEND_PENDING);
        open.setSentAt(null);
        open.setUpdateTime(now);
        registrationRepository.update(open);
        return Prepared.ready(open.getId(), token);
    }

    private static String mailBody(String publicBaseUrl, String token, int ttlMinutes) {
        String link = publicBaseUrl + "/auth/register/verify#t=" + token;
        return """
                请打开下面的链接，并在页面上点击确认，完成注册。链接 %d 分钟内有效，只能确认一次。
                %s

                如果不是你本人操作，请忽略这封邮件。
                """.formatted(ttlMinutes, link);
    }

    private void assertPassword(String rawPassword) {
        EffectivePasswordRules rules = EffectivePasswordRules.from(passwordPolicyConfigApi.get());
        if (!rules.matches(rawPassword)) {
            throw UserException.passwordFormatInvalid(rules.minLength(), rules.maxLength());
        }
    }

    private static PasswordPolicy loginCompatiblePolicy(PasswordPolicy raw) {
        int min = Math.min(
                Math.max(raw.minLength(), EffectivePasswordRules.LOGIN_MIN_LENGTH),
                EffectivePasswordRules.LOGIN_MAX_LENGTH);
        return new PasswordPolicy(
                min,
                EffectivePasswordRules.LOGIN_MAX_LENGTH,
                raw.requireSymbols(),
                raw.allowContainUsername(),
                raw.historyRepetitionTimes(),
                raw.expireDays());
    }

    static String normalizeEmail(String email) {
        String normalized = email == null ? "" : email.trim();
        if (normalized.isEmpty() || normalized.length() > 64 || normalized.indexOf('@') < 1 || normalized.indexOf('@') != normalized.lastIndexOf('@')) {
            throw UserException.of("EMAIL_INVALID", "邮箱格式不正确");
        }
        return normalized;
    }

    static String requirePublicBaseUrl(String publicBaseUrl) {
        String value = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException ex) {
            throw UserException.of("REGISTER_PUBLIC_URL_INVALID", "注册链接地址未正确配置");
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        boolean local = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
        boolean trusted = "https".equalsIgnoreCase(scheme) || ("http".equalsIgnoreCase(scheme) && local);
        if (host == null || scheme == null || uri.getUserInfo() != null || !trusted) {
            throw UserException.of("REGISTER_PUBLIC_URL_INVALID", "注册链接地址未正确配置");
        }
        return value;
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private static Long currentTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId == null ? 0L : tenantId;
    }

    private record Prepared(Long rowId, String token, boolean cooldown, int resendAfterSeconds) {
        static Prepared ready(Long rowId, String token) {
            return new Prepared(rowId, token, false, 0);
        }

        static Prepared cooldown(int seconds) {
            return new Prepared(null, null, true, seconds);
        }
    }
}
