package top.wyhao.system.auth.client;

import org.junit.jupiter.api.Test;
import top.wyhao.system.auth.client.LoginConfigVO;
import top.wyhao.cmn.core.exception.BizException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginConfigVOTest {

    @Test
    void resolvedThreshold_prefersExplicitValue() {
        LoginConfigVO vo = new LoginConfigVO();
        vo.setCaptchaEnabled(false);
        vo.setCaptchaErrorThreshold(3);
        assertThat(vo.resolvedCaptchaErrorThreshold()).isEqualTo(3);
        assertThat(vo.needCaptcha(2)).isFalse();
        assertThat(vo.needCaptcha(3)).isTrue();
    }

    @Test
    void resolvedThreshold_compatDisabledCaptcha() {
        LoginConfigVO vo = new LoginConfigVO();
        vo.setCaptchaEnabled(false);
        assertThat(vo.resolvedCaptchaErrorThreshold()).isEqualTo(-1);
        assertThat(vo.needCaptcha(99)).isFalse();
    }

    @Test
    void resolvedThreshold_defaultWhenMissing() {
        LoginConfigVO vo = new LoginConfigVO();
        assertThat(vo.resolvedCaptchaErrorThreshold()).isEqualTo(2);
        assertThat(vo.isCaptchaAlwaysRequired()).isFalse();
        assertThat(vo.needCaptcha(1)).isFalse();
        assertThat(vo.needCaptcha(2)).isTrue();
    }

    @Test
    void alwaysRequired_whenZero() {
        LoginConfigVO vo = new LoginConfigVO();
        vo.setCaptchaErrorThreshold(0);
        assertThat(vo.isCaptchaAlwaysRequired()).isTrue();
        assertThat(vo.needCaptcha(0)).isTrue();
    }

    @Test
    void validate_rejectsOutOfRange() {
        LoginConfigVO vo = new LoginConfigVO();
        vo.setCaptchaErrorThreshold(6);
        vo.setMaxRetry(10);
        assertThatThrownBy(vo::normalizeAndValidateForWrite)
                .isInstanceOf(BizException.class)
                .extracting(ex -> ((BizException) ex).getCode())
                .isEqualTo("LOGIN_CONFIG_INVALID");
    }

    @Test
    void validate_rejectsThresholdNotLessThanMaxRetry() {
        LoginConfigVO vo = new LoginConfigVO();
        vo.setCaptchaErrorThreshold(5);
        vo.setMaxRetry(5);
        assertThatThrownBy(vo::normalizeAndValidateForWrite)
                .isInstanceOf(BizException.class)
                .extracting(ex -> ((BizException) ex).getCode())
                .isEqualTo("LOGIN_CONFIG_INVALID");
    }

    @Test
    void normalizeForWrite_syncsCaptchaEnabled() {
        LoginConfigVO vo = new LoginConfigVO();
        vo.setCaptchaErrorThreshold(-1);
        vo.setMaxRetry(5);
        vo.normalizeAndValidateForWrite();
        assertThat(vo.getCaptchaEnabled()).isFalse();

        vo.setCaptchaErrorThreshold(2);
        vo.normalizeAndValidateForWrite();
        assertThat(vo.getCaptchaEnabled()).isTrue();
    }
}
