package top.wyhao.system.admin.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import top.wyhao.identity.client.EmailRegistrationConfirm;
import top.wyhao.identity.client.EmailRegistrationStart;
import top.wyhao.identity.client.EmailRegistrationTicket;
import top.wyhao.identity.client.PasswordPolicyConfig;
import top.wyhao.identity.client.PasswordPolicyConfigApi;
import top.wyhao.system.admin.app.service.RegistrationTransactions;
import top.wyhao.system.admin.app.service.TimeSource;
import top.wyhao.system.admin.app.service.UserProfileService;
import top.wyhao.system.admin.domain.exception.UserException;
import top.wyhao.system.admin.domain.gateway.EmailRegistrationRepository;
import top.wyhao.system.admin.domain.gateway.PasswordPolicyGateway;
import top.wyhao.system.admin.domain.gateway.UserRepository;
import top.wyhao.system.admin.domain.model.EmailRegistration;
import top.wyhao.system.admin.domain.model.PasswordPolicy;
import top.wyhao.system.admin.domain.service.RegistrationToken;
import top.wyhao.system.admin.domain.service.UserDomainService;
import top.wyhao.system.cmn.mail.MailClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 邮箱注册状态机。不连数据库、不发真实邮件。
 */
@ExtendWith(MockitoExtension.class)
class EmailRegistrationServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "abc12345";
    private static final String BASE = "https://admin.example.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private UserDomainService userDomainService;
    @Mock
    private PasswordPolicyGateway passwordPolicyGateway;
    @Mock
    private PasswordPolicyConfigApi passwordPolicyConfigApi;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailClient mailClient;

    private final MemoryRegistrations registrations = new MemoryRegistrations();
    private final AtomicReference<LocalDateTime> now = new AtomicReference<>(LocalDateTime.of(2026, 11, 14, 12, 0));
    private EmailRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new EmailRegistrationService(
                registrations,
                userRepository,
                userProfileService,
                userDomainService,
                passwordPolicyGateway,
                passwordPolicyConfigApi,
                passwordEncoder,
                mailClient,
                new InlineTransactions(),
                now::get);
        lenient().when(passwordPolicyConfigApi.get()).thenReturn(new PasswordPolicyConfig());
        lenient().when(passwordPolicyGateway.current()).thenReturn(new PasswordPolicy(8, 32, false, true, 0, 0));
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "{bcrypt}" + invocation.getArgument(0).hashCode());
        lenient().when(userRepository.existsUsername(anyString())).thenReturn(false);
    }

    @Test
    void beginStoresHashAndSendsTheTokenOnce() {
        EmailRegistrationTicket ticket = service.begin(start());

        assertThat(ticket.status()).isEqualTo(EmailRegistrationTicket.PENDING);
        assertThat(ticket.delivered()).isTrue();
        assertThat(ticket.expiresMinutes()).isEqualTo(30);
        EmailRegistration saved = registrations.only();
        assertThat(saved.getPasswordHash()).startsWith("{bcrypt}");
        assertThat(saved.getPasswordHash()).doesNotContain(PASSWORD);
        assertThat(saved.getTokenHash()).isNotBlank();
        assertThat(saved.getTokenHash()).hasSize(64);
        String body = capturedBody();
        assertThat(body).doesNotContain(PASSWORD);
        assertThat(body).contains("/auth/register/verify#t=");
        String token = body.substring(body.indexOf("#t=") + 3, body.indexOf("#t=") + 3 + 43);
        assertThat(saved.getTokenHash()).isEqualTo(RegistrationToken.hash(token.trim()));
        assertThat(body).doesNotContain(saved.getTokenHash());
    }

    @Test
    void failedSendStaysRetryableAndDoesNotClaimDelivery() {
        doThrow(new IllegalStateException("smtp down")).doNothing().when(mailClient).sendText(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> service.begin(start()))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo("REGISTER_MAIL_UNSENT"));
        assertThat(registrations.only().getSendStatus()).isEqualTo(EmailRegistration.SEND_FAILED);

        EmailRegistrationTicket retried = service.begin(start());
        assertThat(retried.delivered()).isTrue();
        verify(mailClient, times(2)).sendText(eq(EMAIL), anyString(), anyString());
    }

    @Test
    void secondRequestInsideTheIntervalDoesNotSendAgain() {
        service.begin(start());
        EmailRegistrationTicket again = service.begin(start());

        assertThat(again.status()).isEqualTo(EmailRegistrationTicket.COOLDOWN);
        assertThat(again.delivered()).isFalse();
        assertThat(again.resendAfterSeconds()).isBetween(1, 60);
        verify(mailClient, times(1)).sendText(anyString(), anyString(), anyString());
    }

    @Test
    void confirmCreatesOneAccountAndReplayDoesNot() {
        service.begin(start());
        String token = tokenFromLastMail();

        EmailRegistrationConfirm first = service.confirm(token, true);
        EmailRegistrationConfirm second = service.confirm(token, true);

        assertThat(first.status()).isEqualTo(EmailRegistrationConfirm.VERIFIED);
        assertThat(second.status()).isEqualTo(EmailRegistrationConfirm.ALREADY_VERIFIED);
        verify(userProfileService, times(1)).createVerifiedEmailAccount(anyString(), eq(EMAIL), anyString(), eq(1L), eq(3L));
    }

    @Test
    void tamperedOrExpiredLinkDoesNotCreateAnAccount() {
        service.begin(start());
        assertThatThrownBy(() -> service.confirm("not-the-token", true))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo("REGISTER_LINK_INVALID"));

        now.set(now.get().plusMinutes(31));
        assertThatThrownBy(() -> service.confirm(tokenFromLastMail(), true))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo("REGISTER_LINK_INVALID"));
        verify(userProfileService, never()).createVerifiedEmailAccount(any(), any(), any(), any(), any());
    }

    @Test
    void existingEmailIsRejectedBeforeMail() {
        when(userRepository.existsEmail(EMAIL, null)).thenReturn(true);

        assertThatThrownBy(() -> service.begin(start()))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo("EMAIL_EXISTS"));
        verify(mailClient, never()).sendText(anyString(), anyString(), anyString());
    }

    @Test
    void confirmOfAnExistingAccountDoesNotCreateAnother() {
        service.begin(start());
        when(userRepository.existsEmail(EMAIL, null)).thenReturn(true);

        EmailRegistrationConfirm result = service.confirm(tokenFromLastMail(), true);

        assertThat(result.status()).isEqualTo(EmailRegistrationConfirm.ALREADY_VERIFIED);
        verify(userProfileService, never()).createVerifiedEmailAccount(any(), any(), any(), any(), any());
    }

    @Test
    void closedRegistrationDoesNotConfirm() {
        service.begin(start());
        assertThatThrownBy(() -> service.confirm(tokenFromLastMail(), false))
                .isInstanceOf(UserException.class)
                .satisfies(ex -> assertThat(((UserException) ex).getCode()).isEqualTo("REGISTRATION_DISABLED"));
        verify(userProfileService, never()).createVerifiedEmailAccount(any(), any(), any(), any(), any());
    }

    @Test
    void publicLinkMustBeHttpsOrLocal() {
        assertThatThrownBy(() -> EmailRegistrationService.requirePublicBaseUrl("http://evil.example"))
                .isInstanceOf(UserException.class);
        assertThat(EmailRegistrationService.requirePublicBaseUrl("http://127.0.0.1:5888/")).isEqualTo("http://127.0.0.1:5888");
        assertThat(EmailRegistrationService.requirePublicBaseUrl("https://admin.example.com")).isEqualTo("https://admin.example.com");
    }

    private EmailRegistrationStart start() {
        return new EmailRegistrationStart(EMAIL, PASSWORD, 1L, 3L, BASE, 30, 60);
    }

    private String capturedBody() {
        org.mockito.ArgumentCaptor<String> body = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(mailClient).sendText(eq(EMAIL), eq("完成注册"), body.capture());
        return body.getValue();
    }

    private String tokenFromLastMail() {
        org.mockito.ArgumentCaptor<String> body = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(mailClient, org.mockito.Mockito.atLeastOnce()).sendText(eq(EMAIL), eq("完成注册"), body.capture());
        String text = body.getValue();
        int start = text.indexOf("#t=") + 3;
        return text.substring(start, text.indexOf('\n', start)).trim();
    }

    private static final class InlineTransactions implements RegistrationTransactions {
        @Override
        public void run(Runnable work) {
            work.run();
        }

        @Override
        public <T> T call(Supplier<T> work) {
            return work.get();
        }
    }

    private static final class MemoryRegistrations implements EmailRegistrationRepository {
        private final List<EmailRegistration> rows = new ArrayList<>();
        private long sequence = 1;

        @Override
        public void deleteStale(LocalDateTime now) {
            rows.removeIf(row -> (!row.consumed() && row.expired(now)) || row.getExpiresAt().isBefore(now.minusDays(1)));
        }

        @Override
        public EmailRegistration findOpen(Long tenantId, String email) {
            return rows.stream()
                    .filter(row -> tenantId.equals(row.getTenantId()) && email.equals(row.getEmail()) && !row.consumed())
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public EmailRegistration lockByTokenHash(String tokenHash) {
            return rows.stream().filter(row -> tokenHash.equals(row.getTokenHash())).findFirst().orElse(null);
        }

        @Override
        public void insert(EmailRegistration registration) {
            registration.setId(sequence++);
            rows.add(registration);
        }

        @Override
        public void update(EmailRegistration registration) {
            // 同一对象已在列表中
        }

        @Override
        public void markSend(Long id, String sendStatus, LocalDateTime sentAt, LocalDateTime now) {
            rows.stream().filter(row -> id.equals(row.getId())).findFirst().ifPresent(row -> {
                row.setSendStatus(sendStatus);
                row.setSentAt(sentAt);
                row.setUpdateTime(now);
            });
        }

        EmailRegistration only() {
            assertThat(rows).hasSize(1);
            return rows.get(0);
        }
    }
}
