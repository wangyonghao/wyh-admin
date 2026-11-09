package top.wyhao.system.admin.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.wyhao.identity.client.PasswordApi;
import top.wyhao.identity.client.PasswordPolicyConfig;
import top.wyhao.identity.client.PasswordPolicyConfigApi;
import top.wyhao.security.client.RoleApi;
import top.wyhao.system.admin.app.service.FileService;
import top.wyhao.system.admin.app.service.MembershipService;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.PasswordPolicyGateway;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.PasswordPolicy;
import top.wyhao.system.admin.domain.model.User;
import top.wyhao.system.admin.domain.service.UserDomainService;
import top.wyhao.system.admin.domain.service.UserLifecycleRules;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 自助注册落库：校验提交的原密码，并用 BCrypt 保存哈希。
 * <p>
 * 使用真实 {@link BCryptPasswordEncoder} 与 {@link UserDomainService}，仓储与角色分配为 Mock。
 * 不起 Spring、不发 HTTP、不写数据库。
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplRegisterTest {

    private static final String SUBMITTED = "abc12345";
    private static final long USER_ID = 42L;
    private static final long DEPARTMENT_ID = 7L;

    @Mock
    private RoleApi roleApi;
    @Mock
    private MembershipService membershipService;
    @Mock
    private PasswordApi passwordApi;
    @Mock
    private FileService fileService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordPolicyConfigApi passwordPolicyConfigApi;
    @Mock
    private PasswordPolicyGateway passwordPolicyGateway;
    @Mock
    private UserLifecycleRules lifecycleRules;

    private PasswordEncoder passwordEncoder;
    private UserProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        UserDomainService userDomainService = new UserDomainService(
                passwordEncoder, userRepository, passwordPolicyGateway);
        service = new UserProfileServiceImpl(
                passwordEncoder,
                roleApi,
                membershipService,
                passwordApi,
                fileService,
                userRepository,
                passwordPolicyConfigApi,
                userDomainService,
                passwordPolicyGateway,
                lifecycleRules);
    }

    @Test
    void registerLocalStoresBcryptOfSubmittedPassword() {
        when(passwordPolicyGateway.current()).thenReturn(policy());
        PasswordPolicyConfig config = new PasswordPolicyConfig();
        config.setPasswordExpireDays(30);
        when(passwordPolicyConfigApi.get()).thenReturn(config);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(USER_ID);
            return null;
        }).when(userRepository).insert(any(User.class));

        User saved = service.registerLocal("alice", SUBMITTED, DEPARTMENT_ID, List.of(3L));

        assertThat(saved.getPassword()).startsWith("$2");
        assertThat(saved.getPassword()).isNotEqualTo(SUBMITTED);
        assertThat(passwordEncoder.matches(SUBMITTED, saved.getPassword())).isTrue();
        assertThat(saved.getPwdExpireDate()).isEqualTo(LocalDate.now().plusDays(30));
        verify(userRepository).insert(saved);
        verify(membershipService).replacePrimaryDepartment(USER_ID, DEPARTMENT_ID);
        verify(roleApi).assignRolesToUser(List.of(3L), USER_ID);
    }

    @Test
    void registerLocalRejectsCiphertextShapedInputBeforeInsert() {
        when(passwordPolicyGateway.current()).thenReturn(policy());

        assertThatThrownBy(() -> service.registerLocal("alice", "A".repeat(88), DEPARTMENT_ID, List.of(3L)))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo("USER_PASSWORD_FORMAT_INVALID"));
        verify(userRepository, never()).insert(any());
    }

    private static PasswordPolicy policy() {
        return new PasswordPolicy(8, 32, false, true, 0, 0);
    }
}
