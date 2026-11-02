package top.wyhao.system.auth.app.command;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.wyhao.cmn.core.constant.SystemConstants;
import top.wyhao.security.client.RoleApi;
import top.wyhao.system.admin.client.PasswordPolicyConfig;
import top.wyhao.system.admin.client.PasswordPolicyConfigApi;
import top.wyhao.system.admin.client.UserApi;
import top.wyhao.system.admin.domain.model.PasswordHistory;
import top.wyhao.system.admin.domain.model.PasswordPolicy;
import top.wyhao.system.admin.domain.service.UserDomainService;
import top.wyhao.system.auth.adapter.client.RegisterConfigApi;
import top.wyhao.system.auth.adapter.client.config.RegisterConfigVO;
import top.wyhao.system.auth.app.dto.AccessTokenResponse;
import top.wyhao.system.auth.app.dto.RegisterRequest;
import top.wyhao.system.auth.domain.exception.AuthenticationException;
import top.wyhao.system.auth.domain.gateway.TokenGateway;
import top.wyhao.system.auth.domain.model.ClientContext;
import top.wyhao.system.auth.domain.user.CredentialUser;
import top.wyhao.web.http.ServletUtils;

import java.util.List;

/**
 * 自助注册并直接登录。
 */
@Component
@RequiredArgsConstructor
public class RegisterCmdExe {

    private static final String DEFAULT_REGISTER_ROLE_CODE = "general";

    private final RegisterConfigApi registerConfigApi;
    private final UserApi userApi;
    private final RoleApi roleApi;
    private final PasswordPolicyConfigApi passwordPolicyConfigApi;
    private final UserDomainService userDomainService;
    private final TokenGateway tokenGateway;

    @Transactional(rollbackFor = Exception.class)
    public AccessTokenResponse execute(@Valid RegisterRequest request) {
        RegisterConfigVO config = registerConfigApi.get();
        if (!config.isRegistrationEnabled()) {
            throw AuthenticationException.registrationDisabled();
        }

        String password = request.getPassword();
        userDomainService.assertCompliant(password, toPolicy(passwordPolicyConfigApi.get()), request.getUsername(),
                PasswordHistory.empty());

        Long roleId = resolveDefaultRoleId(config.getDefaultRoleId());
        if (roleId == null) {
            throw AuthenticationException.registerRoleMissing();
        }
        CredentialUser user = userApi.registerLocalUser(
                request.getUsername(),
                password,
                SystemConstants.SUPER_DEPARTMENT_ID,
                List.of(roleId)
        );

        String ip = ServletUtils.getRequestIp();
        String userAgent = ServletUtils.getRequest() != null
                ? ServletUtils.getRequest().getHeader("User-Agent")
                : null;

        String token = tokenGateway.issue(user,ClientContext.of(ip, userAgent));
        return new AccessTokenResponse("200", token);
    }

    private Long resolveDefaultRoleId(String configuredRoleId) {
        if (StrUtil.isNotBlank(configuredRoleId)) {
            return Convert.toLong(configuredRoleId, null);
        }
        return roleApi.getIdByCode(DEFAULT_REGISTER_ROLE_CODE);
    }

    private PasswordPolicy toPolicy(PasswordPolicyConfig config) {
        if (config == null) {
            return new PasswordPolicy(
                    PasswordPolicy.MIN_LENGTH_LOWER,
                    PasswordPolicy.DEFAULT_MAX_LENGTH,
                    false,
                    false,
                    PasswordPolicy.REPETITION_TIMES_LOWER,
                    90
            );
        }
        return new PasswordPolicy(
                ObjectUtil.defaultIfNull(config.getPasswordMinLength(), PasswordPolicy.MIN_LENGTH_LOWER),
                PasswordPolicy.DEFAULT_MAX_LENGTH,
                Boolean.TRUE.equals(config.getPasswordRequireSpecial()),
                Boolean.TRUE.equals(config.getPasswordAllowContainUsername()),
                ObjectUtil.defaultIfNull(config.getPasswordRepetitionTimes(), PasswordPolicy.REPETITION_TIMES_LOWER),
                ObjectUtil.defaultIfNull(config.getPasswordExpireDays(), 90)
        );
    }
}
