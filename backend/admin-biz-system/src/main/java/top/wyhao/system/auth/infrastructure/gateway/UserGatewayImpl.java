package top.wyhao.system.auth.infrastructure.gateway;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.wyhao.application.autoconfigure.ApplicationProperties;
import top.wyhao.cmn.core.constant.SystemConstants;
import top.wyhao.cmn.core.enums.RoleCodeEnum;
import top.wyhao.notification.client.MessageNotifyApi;
import top.wyhao.system.admin.domain.model.MessageTemplates;
import top.wyhao.security.client.RoleApi;
import top.wyhao.system.admin.adapter.client.DepartmentApi;
import top.wyhao.system.admin.app.service.OrgService;
import top.wyhao.system.auth.domain.user.CredentialUser;
import top.wyhao.system.admin.client.SocialLink;
import top.wyhao.system.admin.client.UserApi;
import top.wyhao.system.auth.domain.gateway.UserGateway;
import top.wyhao.system.auth.domain.user.SocialProfile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {

    private final UserApi userApi;
    private final DepartmentApi departmentApi;
    private final OrgService orgService;
    private final RoleApi roleApi;
    private final MessageNotifyApi messageNotifyApi;
    private final ApplicationProperties applicationProperties;

    @Override
    public CredentialUser findByUsername(String username) {
        return userApi.findByUsername(username);
    }

    @Override
    public CredentialUser findByEmail(String email) {
        return userApi.findByEmail(email);
    }

    @Override
    public CredentialUser findByPhone(String phone) {
        return userApi.findByPhone(phone);
    }

    @Override
    public CredentialUser findById(Long id) {
        return userApi.findById(id);
    }

    @Override
    public SocialLink findSocial(String source, String openId) {
        return userApi.findSocial(source, openId);
    }

    @Override
    public boolean isDepartmentDisabled(Long departmentId) {
        return departmentApi.isDisabled(departmentId);
    }

    @Override
    public Long resolveLoginTenantId(Long userId) {
        return orgService.resolveLoginTenantId(userId);
    }

    @Override
    public CredentialUser registerSocialUser(String username, String displayName, Integer gender) {
        return userApi.registerSocialUser(username, displayName, gender, SystemConstants.SUPER_DEPARTMENT_ID);
    }

    @Override
    public void assignGeneralUserRole(Long userId) {
        Long roleId = roleApi.getIdByCode(RoleCodeEnum.GENERAL_USER.getCode());
        roleApi.assignRolesToUser(Collections.singletonList(roleId), userId);
    }

    private static final int PASSWORD_EXPIRY_WARN_DAYS = 7;

    @Override
    public void notifyWelcome(CredentialUser user) {
        String title = "欢迎加入 %s".formatted(applicationProperties.getName());
        String content = "您好，%s！您已通过第三方账号完成注册。".formatted(user.displayName());
        messageNotifyApi.notifyUsers(title, content, "SECURITY", MessageTemplates.SOCIAL_REGISTER.getPath(),
                CollUtil.toList(user.id().toString()));
    }

    @Override
    public void notifyPasswordExpiryReminder(CredentialUser user) {
        if (user == null || user.id() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        MessageTemplates template;
        String content;
        if (user.isPasswordExpired(today)) {
            template = MessageTemplates.PASSWORD_EXPIRED;
            content = template.getContent();
        } else if (user.isPasswordExpiringSoon(today, PASSWORD_EXPIRY_WARN_DAYS)) {
            template = MessageTemplates.PASSWORD_EXPIRING;
            long daysLeft = ChronoUnit.DAYS.between(today, user.pwdExpireDate());
            content = template.getContent().formatted(Math.max(daysLeft, 0));
        } else {
            return;
        }
        messageNotifyApi.notifyUsers(
                template.getTitle(),
                content,
                "SECURITY",
                template.getPath(),
                CollUtil.toList(user.id().toString()));
    }

    @Override
    public void saveSocialLogin(Long userId, SocialProfile profile) {
        userApi.saveSocialLogin(userId, profile.source(), profile.openId(), profile.rawJson());
    }
}
