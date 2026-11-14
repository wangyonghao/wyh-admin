package top.wyhao.system.auth.app.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.wyhao.cmn.core.constant.SystemConstants;
import top.wyhao.identity.client.EmailRegistrationApi;
import top.wyhao.identity.client.EmailRegistrationStart;
import top.wyhao.identity.client.EmailRegistrationTicket;
import top.wyhao.identity.client.PasswordPolicyConfigApi;
import top.wyhao.identity.client.UserApi;
import top.wyhao.identity.client.UserCredential;
import top.wyhao.security.client.RoleApi;
import top.wyhao.system.auth.app.dto.AccessTokenResponse;
import top.wyhao.system.auth.app.dto.EmailRegistrationResponse;
import top.wyhao.system.auth.app.dto.GrantType;
import top.wyhao.system.auth.app.dto.LoginRequest;
import top.wyhao.system.auth.app.dto.RegisterRequest;
import top.wyhao.system.auth.app.service.RegistrationLinkOrigin;
import top.wyhao.system.auth.client.RegisterConfigApi;
import top.wyhao.system.auth.client.RegisterConfigVO;
import top.wyhao.system.auth.domain.gateway.AuthLogGateway;
import top.wyhao.system.auth.domain.gateway.PasswordFailGateway;
import top.wyhao.system.auth.domain.gateway.SocialAuthGateway;
import top.wyhao.system.auth.domain.gateway.TokenGateway;
import top.wyhao.system.auth.domain.gateway.UserGateway;
import top.wyhao.system.auth.domain.otp.OtpDomainService;
import top.wyhao.system.auth.domain.user.CredentialUser;
import top.wyhao.system.auth.domain.user.PasswordSecurityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 注册与密码登录的口令传递。Mock 协作对象，不起 Spring、不发 HTTP、不写库。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplRegisterTest {

    private static final String SUBMITTED = "abc12345";

    @Mock
    private RegisterConfigApi registerConfigApi;
    @Mock
    private PasswordPolicyConfigApi passwordPolicyConfigApi;
    @Mock
    private UserApi userApi;
    @Mock
    private EmailRegistrationApi emailRegistrationApi;
    @Mock
    private RegistrationLinkOrigin registrationLinkOrigin;
    @Mock
    private RoleApi roleApi;
    @Mock
    private UserGateway userGateway;
    @Mock
    private PasswordSecurityService passwordSecurityService;
    @Mock
    private OtpDomainService otpDomainService;
    @Mock
    private PasswordFailGateway passwordFailGateway;
    @Mock
    private AuthLogGateway authLogGateway;
    @Mock
    private TokenGateway tokenGateway;
    @Mock
    private SocialAuthGateway socialAuthGateway;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerForwardsSubmittedPassword() {
        RegisterConfigVO config = new RegisterConfigVO();
        config.setEnabled(true);
        config.setDefaultRoleId("3");
        when(registerConfigApi.get()).thenReturn(config);
        when(registrationLinkOrigin.requireBaseUrl()).thenReturn("http://127.0.0.1:5888");
        when(emailRegistrationApi.begin(any())).thenReturn(
                new EmailRegistrationTicket(EmailRegistrationTicket.PENDING, true, 60, 30));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("alice@example.com");
        request.setPassword(SUBMITTED);

        EmailRegistrationResponse response = authService.register(request);

        assertThat(response.getStatus()).isEqualTo(EmailRegistrationTicket.PENDING);
        assertThat(response.isDelivered()).isTrue();
        assertThat(response.getCode()).isEqualTo("200");
        ArgumentCaptor<EmailRegistrationStart> start = ArgumentCaptor.forClass(EmailRegistrationStart.class);
        verify(emailRegistrationApi).begin(start.capture());
        assertThat(start.getValue().rawPassword()).isEqualTo(SUBMITTED);
        assertThat(start.getValue().email()).isEqualTo("alice@example.com");
        assertThat(start.getValue().departmentId()).isEqualTo(SystemConstants.SUPER_DEPARTMENT_ID);
        assertThat(start.getValue().roleId()).isEqualTo(3L);
        assertThat(start.getValue().publicBaseUrl()).isEqualTo("http://127.0.0.1:5888");
        verifyNoInteractions(tokenGateway);
        verifyNoInteractions(passwordSecurityService);
    }

    @Test
    void passwordLoginForwardsShortExistingUsername() {
        String shortName = "abc";
        when(userGateway.findByUsername(shortName)).thenReturn(loginUser(shortName));
        when(tokenGateway.issue(any(), any())).thenReturn("token-fixture");

        LoginRequest request = new LoginRequest();
        request.setClientId("client");
        request.setUsername(shortName);
        request.setPassword(SUBMITTED);

        AccessTokenResponse response = authService.login(GrantType.PASSWORD, request);

        assertThat(response.getToken()).isEqualTo("token-fixture");
        verify(passwordSecurityService).fastVerifyPasswordFormat(eq(shortName), eq(SUBMITTED), any());
        verify(passwordSecurityService).verifyPassword(eq(loginUser(shortName)), eq(SUBMITTED), any());
    }

    @Test
    void resolveUsernameKeepsWriteLength() {
        assertThat(authService.resolveUsername("abc", false, "fallback")).isEqualTo("fallback");
        assertThat(authService.resolveUsername("abcd", false, "fallback")).isEqualTo("abcd");
        String sixtyFour = "a" + "b".repeat(63);
        String sixtyFive = "a" + "b".repeat(64);
        assertThat(authService.resolveUsername(sixtyFour, false, "fallback")).isEqualTo(sixtyFour);
        assertThat(authService.resolveUsername(sixtyFive, false, "fallback")).isEqualTo("fallback");
    }

    @Test
    void passwordLoginForwardsSubmittedPassword() {
        when(userGateway.findByUsername("alice")).thenReturn(loginUser("alice"));
        when(tokenGateway.issue(any(), any())).thenReturn("token-fixture");

        LoginRequest request = new LoginRequest();
        request.setClientId("client");
        request.setUsername("alice");
        request.setPassword(SUBMITTED);

        AccessTokenResponse response = authService.login(GrantType.PASSWORD, request);

        assertThat(response.getToken()).isEqualTo("token-fixture");
        verify(passwordSecurityService).fastVerifyPasswordFormat(eq("alice"), eq(SUBMITTED), any());
        verify(passwordSecurityService).verifyPassword(eq(loginUser("alice")), eq(SUBMITTED), any());
    }

    @Test
    void passwordLoginUsesExistingEmailField() {
        when(userGateway.findByEmail("alice@example.com")).thenReturn(loginUser("alice"));
        when(tokenGateway.issue(any(), any())).thenReturn("token-fixture");

        LoginRequest request = new LoginRequest();
        request.setClientId("client");
        request.setEmail("alice@example.com");
        request.setPassword(SUBMITTED);

        AccessTokenResponse response = authService.login(GrantType.PASSWORD, request);

        assertThat(response.getToken()).isEqualTo("token-fixture");
        verify(userGateway).findByEmail("alice@example.com");
        verify(passwordSecurityService).verifyPassword(eq(loginUser("alice")), eq(SUBMITTED), any());
    }

    private static UserCredential credential() {
        return new UserCredential(
                9L,
                "alice",
                "alice",
                "stored-hash",
                1,
                0,
                SystemConstants.SUPER_DEPARTMENT_ID,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDate.of(2030, 1, 1));
    }

    private static CredentialUser loginUser(String username) {
        return new CredentialUser(
                1L,
                username,
                "Alice",
                "stored-hash",
                1,
                0,
                10L,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDate.of(2030, 1, 1));
    }
}
