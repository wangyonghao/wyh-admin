package top.wyhao.system.admin.app.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.system.cmn.mail.MailClient;
import top.wyhao.cmn.core.enums.RoleCodeEnum;
import top.wyhao.satoken.util.LoginUtil;
import top.wyhao.system.admin.app.dto.OrgApplyRequest;
import top.wyhao.system.admin.app.dto.OrgCreateRequest;
import top.wyhao.system.admin.app.dto.OrgInviteRequest;
import top.wyhao.system.admin.app.dto.OrgInviteResult;
import top.wyhao.system.admin.app.dto.OrgResult;
import top.wyhao.system.admin.app.dto.OrgSessionResult;
import top.wyhao.system.admin.app.service.MembershipService;
import top.wyhao.system.admin.app.service.OrgService;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.system.admin.infrastructure.config.OrgProperties;
import top.wyhao.system.admin.domain.exception.OrgException;
import top.wyhao.system.admin.domain.model.OrgApplicationStatus;
import top.wyhao.system.admin.domain.model.OrgInviteStatus;
import top.wyhao.system.admin.domain.model.Membership;
import top.wyhao.system.admin.domain.model.OrgApplication;
import top.wyhao.system.admin.domain.model.OrgInvite;
import top.wyhao.system.admin.domain.gateway.OrgApplicationRepository;
import top.wyhao.system.admin.domain.gateway.OrgInviteRepository;
import top.wyhao.tenant.client.TenantApi;
import top.wyhao.tenant.client.TenantBO;
import top.wyhao.tenant.config.TenantProperties;
import top.wyhao.tenant.context.TenantContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final MembershipService membershipService;
    private final TenantApi tenantApi;
    private final TenantProperties tenantProperties;
    private final OrgProperties orgProperties;
    private final OrgInviteRepository inviteRepository;
    private final OrgApplicationRepository applicationRepository;
    private final MailClient mailClient;
    private final UserRepository userRepository;

    @Override
    public Long resolveLoginTenantId(Long userId) {
        // 兼容旧数据：用户行上已有非默认租户、但尚无 TENANT 成员关系时，补一条主组织成员关系
        User user = userRepository.findById(userId);
        if (user != null && user.getTenantId() != null
                && !Objects.equals(user.getTenantId(), tenantProperties.getDefaultTenantId())
                && !membershipService.isTenantMember(userId, user.getTenantId())) {
            membershipService.ensureTenantMembership(userId, user.getTenantId(), true);
        }

        Long primary = membershipService.resolvePrimaryTenantId(userId);
        if (primary != null) {
            return primary;
        }
        // 仅超级管理员在无组织时进入平台默认租户；其他人进入无组织引导
        if (LoginUtil.isSuperadmin()) {
            return tenantProperties.getDefaultTenantId();
        }
        return null;
    }

    @Override
    public OrgSessionResult currentSession() {
        Long userId = LoginUtil.getUserId();
        OrgSessionResult result = new OrgSessionResult();
        List<Membership> memberships = membershipService.listEffectiveTenantMemberships(userId);
        List<Long> orgIds = memberships.stream().map(Membership::getScopeId).toList();
        Map<Long, TenantBO> tenants = tenantApi.listByIds(orgIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TenantBO::getId, Function.identity(), (a, b) -> a));

        Long currentId = LoginUtil.getTenantId();
        List<OrgResult> orgs = new ArrayList<>();
        for (Membership m : memberships) {
            TenantBO t = tenants.get(m.getScopeId());
            if (t == null) {
                continue;
            }
            OrgResult org = new OrgResult(t.getId(), t.getName(), t.getCode(), Boolean.TRUE.equals(m.getIsPrimary()));
            orgs.add(org);
            if (Objects.equals(t.getId(), currentId)) {
                result.setCurrent(org);
            }
        }
        result.setOrganizations(orgs);
        if (result.getCurrent() == null && !orgs.isEmpty()) {
            result.setCurrent(orgs.get(0));
        }
        result.setNeedsOrganization(orgs.isEmpty() && !LoginUtil.isSuperadmin());
        result.setPendingInvites(listPendingInvitesForCurrentUser());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrgResult create(OrgCreateRequest request) {
        Long userId = LoginUtil.getUserId();
        Long orgId = tenantApi.createOwnedBy(request.getName(), request.getDescription(), userId);
        membershipService.ensureTenantMembership(userId, orgId, true);
        switchOrg(orgId);
        TenantBO tenant = tenantApi.get(orgId);
        return new OrgResult(tenant.getId(), tenant.getName(), tenant.getCode(), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchOrg(Long orgId) {
        Long userId = LoginUtil.getUserId();
        if (!membershipService.isTenantMember(userId, orgId) && !LoginUtil.isSuperadmin()) {
            throw OrgException.notMember();
        }
        if (!LoginUtil.isSuperadmin()) {
            membershipService.setPrimaryTenant(userId, orgId);
        } else if (membershipService.isTenantMember(userId, orgId)) {
            membershipService.setPrimaryTenant(userId, orgId);
        }
        LoginUtil.setTenantId(orgId);
        top.wyhao.tenant.context.TenantContext context = new top.wyhao.tenant.context.TenantContext();
        context.setTenantId(orgId);
        TenantContextHolder.setContext(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(OrgApplyRequest request) {
        Long userId = LoginUtil.getUserId();
        TenantBO tenant = tenantApi.findByCode(request.getCode());
        if (tenant == null) {
            throw OrgException.codeInvalid(request.getCode());
        }
        if (membershipService.isTenantMember(userId, tenant.getId())) {
            throw OrgException.alreadyMember();
        }
        boolean pending = applicationRepository.existsPending(tenant.getId(), userId);
        if (pending) {
            throw OrgException.applicationPending();
        }
        OrgApplication application = new OrgApplication();
        application.setTenantId(tenant.getId());
        application.setUserId(userId);
        application.setMessage(request.getMessage());
        application.setStatus(OrgApplicationStatus.PENDING.getValue());
        application.setDeleted(0);
        applicationRepository.insert(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrgInviteResult invite(OrgInviteRequest request) {
        Long userId = LoginUtil.getUserId();
        Long tenantId = LoginUtil.getTenantId();
        if (tenantId == null || Objects.equals(tenantId, tenantProperties.getDefaultTenantId())) {
            throw OrgException.forbidden();
        }
        if (!LoginUtil.isSuperadmin() && !LoginUtil.hasRole(RoleCodeEnum.TENANT_ADMIN.getCode())) {
            throw OrgException.forbidden();
        }
        String email = normalizeEmail(request.getEmail());
        // 幂等：同组织同邮箱的待处理邀请复用
        OrgInvite existing = inviteRepository.findPendingByTenantAndEmail(tenantId, email);
        String rawToken;
        OrgInvite invite;
        if (existing != null && existing.getExpireTime() != null && existing.getExpireTime().isAfter(LocalDateTime.now())) {
            invite = existing;
            // 已有待处理邀请：重新发信需要新 token（旧链接仍可用到过期）
            rawToken = IdUtil.fastSimpleUUID() + IdUtil.fastSimpleUUID();
            invite.setTokenHash(DigestUtil.sha256Hex(rawToken));
            invite.setExpireTime(LocalDateTime.now().plusDays(orgProperties.getInviteExpireDays()));
            inviteRepository.updateById(invite);
        } else {
            if (existing != null) {
                existing.setStatus(OrgInviteStatus.EXPIRED.getValue());
                inviteRepository.updateById(existing);
            }
            rawToken = IdUtil.fastSimpleUUID() + IdUtil.fastSimpleUUID();
            invite = new OrgInvite();
            invite.setTenantId(tenantId);
            invite.setEmail(email);
            invite.setTokenHash(DigestUtil.sha256Hex(rawToken));
            invite.setStatus(OrgInviteStatus.PENDING.getValue());
            invite.setInvitedBy(userId);
            invite.setExpireTime(LocalDateTime.now().plusDays(orgProperties.getInviteExpireDays()));
            invite.setDeleted(0);
            inviteRepository.insert(invite);
        }

        TenantBO tenant = tenantApi.get(tenantId);
        String link = orgProperties.getInviteBaseUrl() + "?token=" + rawToken;
        String subject = "邀请你加入组织「" + (tenant != null ? tenant.getName() : tenantId) + "」";
        String content = "你好，\n\n你被邀请加入组织「"
                + (tenant != null ? tenant.getName() : tenantId)
                + "」。\n请打开以下链接，登录或注册后确认或拒绝邀请：\n"
                + link
                + "\n\n链接将于 "
                + invite.getExpireTime()
                + " 过期。\n若非本人操作请忽略本邮件。";
        try {
            mailClient.sendText(email, subject, content);
        } catch (Exception ex) {
            log.error("发送组织邀请邮件失败: email={}", email, ex);
            throw OrgException.of("ORG_INVITE_MAIL_FAILED", "邀请邮件发送失败: " + ex.getMessage());
        }

        OrgInviteResult result = toInviteResult(invite, tenant);
        result.setToken(rawToken);
        return result;
    }

    @Override
    public OrgInviteResult previewInvite(String rawToken) {
        OrgInvite invite = requireInvite(rawToken);
        return toInviteResult(invite, tenantApi.get(invite.getTenantId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptInvite(String rawToken) {
        Long userId = LoginUtil.getUserId();
        OrgInvite invite = requireInvite(rawToken);
        User user = userRepository.findById(userId);
        String userEmail = user != null ? user.getEmail() : null;
        if (StrUtil.isBlank(userEmail)) {
            throw OrgException.emailRequired();
        }
        if (!normalizeEmail(userEmail).equals(invite.getEmail())) {
            throw OrgException.inviteEmailMismatch();
        }
        membershipService.ensureTenantMembership(userId, invite.getTenantId(),
                membershipService.resolvePrimaryTenantId(userId) == null);
        invite.setStatus(OrgInviteStatus.ACCEPTED.getValue());
        invite.setAcceptedUserId(userId);
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepository.updateById(invite);
        switchOrg(invite.getTenantId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectInvite(String rawToken) {
        OrgInvite invite = requireInvite(rawToken);
        Long userId = LoginUtil.getUserId();
        User user = userRepository.findById(userId);
        String userEmail = user != null ? user.getEmail() : null;
        if (StrUtil.isBlank(userEmail) || !normalizeEmail(userEmail).equals(invite.getEmail())) {
            throw OrgException.inviteEmailMismatch();
        }
        invite.setStatus(OrgInviteStatus.REJECTED.getValue());
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepository.updateById(invite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewApplication(Long applicationId, boolean approve) {
        if (!LoginUtil.isSuperadmin() && !LoginUtil.hasRole(RoleCodeEnum.TENANT_ADMIN.getCode())) {
            throw OrgException.forbidden();
        }
        OrgApplication application = applicationRepository.findById(applicationId);
        if (application == null || application.getDeleted() != null && application.getDeleted() != 0) {
            throw OrgException.applicationNotFound();
        }
        if (!Objects.equals(application.getTenantId(), LoginUtil.getTenantId()) && !LoginUtil.isSuperadmin()) {
            throw OrgException.forbidden();
        }
        if (application.getStatus() == null || application.getStatus() != OrgApplicationStatus.PENDING.getValue()) {
            throw OrgException.applicationHandled();
        }
        application.setReviewedBy(LoginUtil.getUserId());
        application.setReviewedAt(LocalDateTime.now());
        if (approve) {
            application.setStatus(OrgApplicationStatus.APPROVED.getValue());
            membershipService.ensureTenantMembership(application.getUserId(), application.getTenantId(), false);
        } else {
            application.setStatus(OrgApplicationStatus.REJECTED.getValue());
        }
        applicationRepository.updateById(application);
    }

    private List<OrgInviteResult> listPendingInvitesForCurrentUser() {
        User user = userRepository.findById(LoginUtil.getUserId());
        if (user == null || StrUtil.isBlank(user.getEmail())) {
            return List.of();
        }
        String email = normalizeEmail(user.getEmail());
        List<OrgInvite> invites = inviteRepository.listPendingByEmail(email, LocalDateTime.now());
        Map<Long, TenantBO> tenants = tenantApi.listByIds(invites.stream().map(OrgInvite::getTenantId).toList())
                .stream().collect(Collectors.toMap(TenantBO::getId, Function.identity(), (a, b) -> a));
        return invites.stream().map(i -> toInviteResult(i, tenants.get(i.getTenantId()))).toList();
    }

    private OrgInvite requireInvite(String rawToken) {
        if (StrUtil.isBlank(rawToken)) {
            throw OrgException.inviteNotFound();
        }
        String hash = DigestUtil.sha256Hex(rawToken.trim());
        OrgInvite invite = inviteRepository.findByTokenHash(hash);
        if (invite == null) {
            throw OrgException.inviteNotFound();
        }
        if (invite.getStatus() != null && invite.getStatus() != OrgInviteStatus.PENDING.getValue()) {
            throw OrgException.inviteHandled();
        }
        if (invite.getExpireTime() != null && invite.getExpireTime().isBefore(LocalDateTime.now())) {
            invite.setStatus(OrgInviteStatus.EXPIRED.getValue());
            inviteRepository.updateById(invite);
            throw OrgException.inviteExpired();
        }
        return invite;
    }

    private OrgInviteResult toInviteResult(OrgInvite invite, TenantBO tenant) {
        OrgInviteResult result = new OrgInviteResult();
        result.setId(invite.getId());
        result.setTenantId(invite.getTenantId());
        result.setEmail(invite.getEmail());
        result.setStatus(invite.getStatus());
        result.setExpireTime(invite.getExpireTime());
        if (tenant != null) {
            result.setOrgName(tenant.getName());
            result.setOrgCode(tenant.getCode());
        }
        return result;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
