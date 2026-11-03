package top.wyhao.system.auth.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.wyhao.system.auth.domain.exception.AuthenticationException;
import top.wyhao.system.auth.domain.gateway.CaptchaGateway;
import top.wyhao.system.auth.domain.gateway.PasswordEncodeGateway;
import top.wyhao.system.auth.domain.gateway.PasswordFailGateway;
import top.wyhao.system.auth.domain.model.ClientContext;
import top.wyhao.system.auth.domain.model.ImageCaptcha;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link PasswordSecurityService} 单测：失败计数、锁定、图验阈值、慢哈希校验。
 * <p>
 * 断言用 {@code getCode()}/{@code getDefaultMessage()}/{@code getRealReason()}，
 * 避免 {@code getMessage()} 触发 I18n（需 Spring）。
 */
@ExtendWith(MockitoExtension.class)
class PasswordSecurityServiceTest {

    private static final String ACCOUNT = "alice";
    private static final String IP = "10.0.0.8";
    private static final ClientContext CLIENT = ClientContext.of(IP, "JUnit");

    @Mock
    private PasswordFailGateway failCountGateway;
    @Mock
    private PasswordEncodeGateway passwordEncodeGateway;
    @Mock
    private CaptchaGateway captchaGateway;

    @InjectMocks
    private PasswordSecurityService service;

    private static PasswordPolicy policy(int maxRetry, int lockMinutes, int captchaThreshold) {
        return new PasswordPolicy(maxRetry, lockMinutes, captchaThreshold);
    }

    private static CredentialUser user(String username, String encodedPassword) {
        return new CredentialUser(
                1L,
                username,
                "Alice",
                encodedPassword,
                1,
                0,
                10L,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDate.of(2030, 1, 1));
    }

    @Nested
    class NeedCaptcha {

        @Test
        void falseWhenBelowThreshold() {
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(1);
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));

            assertThat(service.needCaptcha(ACCOUNT)).isFalse();
        }

        @Test
        void trueWhenAtOrAboveThreshold() {
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(2);
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));

            assertThat(service.needCaptcha(ACCOUNT)).isTrue();
        }

        @Test
        void falseWhenFeatureDisabledByNegativeThreshold() {
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(99);
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, -1));

            assertThat(service.needCaptcha(ACCOUNT)).isFalse();
        }

        @Test
        void alwaysTrueWhenThresholdZero() {
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(0);
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 0));

            assertThat(service.needCaptcha(ACCOUNT)).isTrue();
        }
    }

    @Nested
    class VerifyCaptchaIfNeeded {

        @Test
        void skipsGatewayWhenNotNeeded() {
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(0);
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));

            assertThatCode(() -> service.verifyCaptchaIfNeeded(ACCOUNT, "uuid", "abcd"))
                    .doesNotThrowAnyException();
            verifyNoInteractions(captchaGateway);
        }

        @Test
        void delegatesToGatewayWhenNeeded() {
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(2);
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));

            service.verifyCaptchaIfNeeded(ACCOUNT, "uuid-1", "code");

            verify(captchaGateway).verify("uuid-1", "code");
        }

        @Test
        void propagatesGatewayFailure() {
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(2);
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));
            doThrow(AuthenticationException.captchaIncorrect()).when(captchaGateway).verify("u", "bad");

            assertThatThrownBy(() -> service.verifyCaptchaIfNeeded(ACCOUNT, "u", "bad"))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> assertThat(((AuthenticationException) ex).getCode())
                            .isEqualTo("AUTH_CAPTCHA_INCORRECT"));
        }
    }

    @Nested
    class AssertNotBanned {

        @Test
        void passesWhenNeitherBanned() {
            when(failCountGateway.isBanned(PasswordFailGateway.LimitDimension.IP, IP)).thenReturn(false);
            when(failCountGateway.isBanned(PasswordFailGateway.LimitDimension.ACCOUNT, ACCOUNT)).thenReturn(false);

            assertThatCode(() -> service.assertNotBanned(ACCOUNT, IP)).doesNotThrowAnyException();
        }

        @Test
        void rejectsWhenIpBanned() {
            when(failCountGateway.isBanned(PasswordFailGateway.LimitDimension.IP, IP)).thenReturn(true);

            assertThatThrownBy(() -> service.assertNotBanned(ACCOUNT, IP))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> {
                        AuthenticationException ae = (AuthenticationException) ex;
                        assertThat(ae.getCode()).isEqualTo("TOO_MANY_REQUEST");
                        assertThat(ae.getRealReason()).contains(ACCOUNT).contains(IP);
                    });
            verify(failCountGateway, never()).isBanned(eq(PasswordFailGateway.LimitDimension.ACCOUNT), anyString());
        }

        @Test
        void rejectsWhenAccountBanned() {
            when(failCountGateway.isBanned(PasswordFailGateway.LimitDimension.IP, IP)).thenReturn(false);
            when(failCountGateway.isBanned(PasswordFailGateway.LimitDimension.ACCOUNT, ACCOUNT)).thenReturn(true);
            when(failCountGateway.getRemainMinutes(PasswordFailGateway.LimitDimension.ACCOUNT, ACCOUNT)).thenReturn(12);

            assertThatThrownBy(() -> service.assertNotBanned(ACCOUNT, IP))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> {
                        AuthenticationException ae = (AuthenticationException) ex;
                        assertThat(ae.getCode()).isEqualTo("ACCOUNT_LOCKED");
                        assertThat(ae.getDefaultMessage()).contains("12");
                    });
        }
    }

    @Nested
    class CreateCaptcha {

        @Test
        void returnsDisabledWhenFeatureOff() {
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, -1));

            ImageCaptcha captcha = service.createCaptcha();

            assertThat(captcha.uuid()).isNull();
            assertThat(captcha.img()).isNull();
            assertThat(captcha.alwaysRequired()).isFalse();
            verifyNoInteractions(captchaGateway);
        }

        @Test
        void generatesAndMarksAlwaysRequiredWhenThresholdZero() {
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 0));
            when(captchaGateway.generate()).thenReturn(new ImageCaptcha("u1", "img-data", 60L, false));

            ImageCaptcha captcha = service.createCaptcha();

            assertThat(captcha.uuid()).isEqualTo("u1");
            assertThat(captcha.img()).isEqualTo("img-data");
            assertThat(captcha.expireTime()).isEqualTo(60L);
            assertThat(captcha.alwaysRequired()).isTrue();
        }

        @Test
        void generatesWithoutAlwaysRequiredWhenThresholdPositive() {
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));
            when(captchaGateway.generate()).thenReturn(new ImageCaptcha("u2", "png", 120L, false));

            ImageCaptcha captcha = service.createCaptcha();

            assertThat(captcha.alwaysRequired()).isFalse();
            assertThat(captcha.uuid()).isEqualTo("u2");
        }
    }

    @Nested
    class VerifyPassword {

        @Test
        void clearsFailCountWhenMatches() {
            CredentialUser credential = user(ACCOUNT, "encoded");
            when(passwordEncodeGateway.matches("abc12345", "encoded")).thenReturn(true);

            assertThatCode(() -> service.verifyPassword(credential, "abc12345", CLIENT))
                    .doesNotThrowAnyException();
            verify(failCountGateway).clear(ACCOUNT);
            verify(failCountGateway, never()).increment(anyString(), anyString(), anyInt());
        }

        @Test
        void rejectsWhenMismatch() {
            CredentialUser credential = user(ACCOUNT, "encoded");
            when(passwordEncodeGateway.matches("wrong-pass", "encoded")).thenReturn(false);
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));
            when(failCountGateway.increment(ACCOUNT, IP, 30)).thenReturn(1);
            when(failCountGateway.isBanned(any(), any())).thenReturn(false);
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(1);

            assertThatThrownBy(() -> service.verifyPassword(credential, "wrong-pass", CLIENT))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> {
                        AuthenticationException ae = (AuthenticationException) ex;
                        assertThat(ae.getCode()).isEqualTo("PASSWORD_ERROR");
                        assertThat(ae.getDefaultMessage()).isEqualTo("账号或密码错误");
                        assertThat(ae.getRealReason()).isEqualTo("密码不匹配");
                    });
            verify(failCountGateway).increment(ACCOUNT, IP, 30);
            verify(failCountGateway, never()).clear(anyString());
        }
    }

    @Nested
    class FastVerifyPasswordFormat {

        @Test
        void passesCompliantPassword() {
            assertThatCode(() -> service.fastVerifyPasswordFormat(ACCOUNT, "abc12345", CLIENT))
                    .doesNotThrowAnyException();
            verifyNoInteractions(failCountGateway);
        }

        @Test
        void rejectsInvalidFormatAndCounts() {
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));
            when(failCountGateway.increment(ACCOUNT, IP, 30)).thenReturn(1);
            when(failCountGateway.isBanned(any(), any())).thenReturn(false);
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(1);

            assertThatThrownBy(() -> service.fastVerifyPasswordFormat(ACCOUNT, "short", CLIENT))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> {
                        AuthenticationException ae = (AuthenticationException) ex;
                        assertThat(ae.getCode()).isEqualTo("PASSWORD_ERROR");
                        assertThat(ae.getRealReason()).isEqualTo("密码格式错误");
                    });
            verify(failCountGateway).increment(ACCOUNT, IP, 30);
        }
    }

    @Nested
    class RejectCredential {

        @BeforeEach
        void stubPolicy() {
            when(failCountGateway.currentPolicy()).thenReturn(policy(5, 30, 2));
        }

        @Test
        void incrementsEvenWhenUserNotFoundStyleReason() {
            when(failCountGateway.increment(ACCOUNT, IP, 30)).thenReturn(1);
            when(failCountGateway.isBanned(any(), any())).thenReturn(false);
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(1);

            assertThatThrownBy(() -> service.rejectCredential(ACCOUNT, CLIENT, "用户不存在"))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> {
                        AuthenticationException ae = (AuthenticationException) ex;
                        assertThat(ae.getCode()).isEqualTo("PASSWORD_ERROR");
                        assertThat(ae.getDefaultMessage()).isEqualTo("账号或密码错误");
                        assertThat(ae.getRealReason()).isEqualTo("用户不存在");
                    });
            verify(failCountGateway).increment(ACCOUNT, IP, 30);
        }

        @Test
        void throwsNeedCaptchaWhenFailCountReachesThreshold() {
            when(failCountGateway.increment(ACCOUNT, IP, 30)).thenReturn(2);
            when(failCountGateway.isBanned(any(), any())).thenReturn(false);
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(2);

            assertThatThrownBy(() -> service.rejectCredential(ACCOUNT, CLIENT, "密码不匹配"))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> {
                        AuthenticationException ae = (AuthenticationException) ex;
                        assertThat(ae.getCode()).isEqualTo("NEED_CAPTCHA");
                        assertThat(ae.getRealReason()).isEqualTo("密码不匹配");
                    });
        }

        @Test
        void locksAccountWhenAccountBannedAfterIncrement() {
            when(failCountGateway.increment(ACCOUNT, IP, 30)).thenReturn(5);
            when(failCountGateway.isBanned(PasswordFailGateway.LimitDimension.IP, IP)).thenReturn(false);
            when(failCountGateway.isBanned(PasswordFailGateway.LimitDimension.ACCOUNT, ACCOUNT)).thenReturn(true);
            when(failCountGateway.getRemainMinutes(PasswordFailGateway.LimitDimension.ACCOUNT, ACCOUNT)).thenReturn(30);

            assertThatThrownBy(() -> service.rejectCredential(ACCOUNT, CLIENT, "密码不匹配"))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> {
                        AuthenticationException ae = (AuthenticationException) ex;
                        assertThat(ae.getCode()).isEqualTo("ACCOUNT_LOCKED");
                        assertThat(ae.getRealReason()).isEqualTo("密码不匹配");
                        assertThat(ae.getDefaultMessage()).contains("30");
                    });
            verify(failCountGateway, never()).getFailCount(anyString());
        }

        @Test
        void rejectsByIpBanPreferentially() {
            when(failCountGateway.increment(ACCOUNT, IP, 30)).thenReturn(5);
            when(failCountGateway.isBanned(PasswordFailGateway.LimitDimension.IP, IP)).thenReturn(true);

            assertThatThrownBy(() -> service.rejectCredential(ACCOUNT, CLIENT, "用户不存在"))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> {
                        AuthenticationException ae = (AuthenticationException) ex;
                        assertThat(ae.getCode()).isEqualTo("TOO_MANY_REQUEST");
                        assertThat(ae.getRealReason()).isEqualTo("用户不存在");
                    });
            verify(failCountGateway, never()).isBanned(eq(PasswordFailGateway.LimitDimension.ACCOUNT), anyString());
        }

        @Test
        void allowsNullClientAndStillIncrements() {
            when(failCountGateway.increment(eq(ACCOUNT), isNull(), eq(30))).thenReturn(1);
            when(failCountGateway.isBanned(any(), isNull())).thenReturn(false);
            when(failCountGateway.isBanned(any(), eq(ACCOUNT))).thenReturn(false);
            when(failCountGateway.getFailCount(ACCOUNT)).thenReturn(0);

            assertThatThrownBy(() -> service.rejectCredential(ACCOUNT, null, "用户不存在"))
                    .isInstanceOf(AuthenticationException.class)
                    .satisfies(ex -> assertThat(((AuthenticationException) ex).getCode()).isEqualTo("PASSWORD_ERROR"));
            verify(failCountGateway).increment(ACCOUNT, null, 30);
        }
    }
}
