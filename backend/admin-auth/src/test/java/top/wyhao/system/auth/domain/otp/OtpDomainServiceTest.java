package top.wyhao.system.auth.domain.otp;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.wyhao.security.client.otp.OtpScene;
import top.wyhao.system.auth.domain.gateway.OtpDeliveryGateway;
import top.wyhao.system.auth.domain.gateway.OtpGateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpDomainServiceTest {

    @Mock
    private OtpGateway otpGateway;
    @Mock
    private OtpDeliveryGateway otpDeliveryGateway;

    @InjectMocks
    private OtpDomainService service;

    @Nested
    class Issue {

        @Test
        void generatesOnceAndPersistsSameCode() {
            when(otpGateway.codeLength()).thenReturn(6);
            when(otpGateway.expireInSeconds()).thenReturn(300);

            Otp otp = service.issue("a@b.com");

            assertThat(otp.code()).hasSize(6);
            assertThat(otp.uuid()).isNotBlank();
            assertThat(otp.expireSeconds()).isEqualTo(300);
            assertThat(otp.expireMinutes()).isEqualTo(5);

            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(otpGateway).save(eq("a@b.com"), codeCaptor.capture(), eq(300L));
            assertThat(codeCaptor.getValue()).isEqualTo(otp.code());
            verify(otpGateway).saveSession(eq(otp.uuid()), eq("a@b.com"), eq(300L));
        }
    }

    @Nested
    class Send {

        @Test
        void emailUsesSameCodeFromOtp() {
            Otp otp = new Otp("a@b.com", "uuid-1", "482917", 300, java.time.LocalDateTime.now().plusSeconds(300));

            service.sendEmail("a@b.com", OtpScene.LOGIN, "zh_CN", otp, "127.0.0.1");

            verify(otpDeliveryGateway).sendEmail(
                    eq("a@b.com"), eq(OtpScene.LOGIN), eq("zh_CN"),
                    eq("482917"), eq(5), eq("127.0.0.1"));
        }

        @Test
        void smsUsesSameCodeFromOtp() {
            Otp otp = new Otp("13800138000", "uuid-2", "482917", 300, java.time.LocalDateTime.now().plusSeconds(300));

            service.sendSms("13800138000", OtpScene.LOGIN, otp);

            verify(otpDeliveryGateway).sendSms(eq("13800138000"), eq(OtpScene.LOGIN), eq("482917"), eq(5));
        }
    }

    @Nested
    class Verify {

        @Test
        void successConsumesCodeAndSession() {
            when(otpGateway.get("a@b.com")).thenReturn("482917");
            when(otpGateway.getContactBySession("uuid-1")).thenReturn("a@b.com");

            service.verify("a@b.com", "482917", "uuid-1");

            verify(otpGateway).delete("a@b.com");
            verify(otpGateway).deleteSession("uuid-1");
        }

        @Test
        void wrongCodeRejectedWithoutDelete() {
            when(otpGateway.get("a@b.com")).thenReturn("482917");

            assertThatThrownBy(() -> service.verify("a@b.com", "000000", "uuid-1"))
                    .isInstanceOf(OtpException.class)
                    .extracting(ex -> ((OtpException) ex).getCode())
                    .isEqualTo("OTP_INVALID");

            verify(otpGateway, never()).delete(anyString());
            verify(otpGateway, never()).deleteSession(anyString());
        }

        @Test
        void missingCachedCodeTreatedAsExpired() {
            when(otpGateway.get("a@b.com")).thenReturn(null);

            assertThatThrownBy(() -> service.verify("a@b.com", "482917", null))
                    .isInstanceOf(OtpException.class)
                    .extracting(ex -> ((OtpException) ex).getCode())
                    .isEqualTo("OTP_EXPIRED");
        }

        @Test
        void mismatchedUuidRejected() {
            when(otpGateway.get("a@b.com")).thenReturn("482917");
            when(otpGateway.getContactBySession("bad-uuid")).thenReturn("other@b.com");

            assertThatThrownBy(() -> service.verify("a@b.com", "482917", "bad-uuid"))
                    .isInstanceOf(OtpException.class)
                    .extracting(ex -> ((OtpException) ex).getCode())
                    .isEqualTo("OTP_NOT_FOUND");

            verify(otpGateway, never()).delete(anyString());
        }
    }
}
