package top.wyhao.cmn.core.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 业务异常在没有对应 i18n 条目时，应把创建时的默认文案返回给调用方，而不是把错误码直接展示给用户。
 */
class BizExceptionTest {

    @Test
    void fallsBackToDefaultMessageWhenI18nKeyMissing() {
        BizException ex = new BizException("EMAIL_EXISTS", "邮箱已被占用");
        assertThat(ex.getCode()).isEqualTo("EMAIL_EXISTS");
        assertThat(ex.getMessage()).isEqualTo("邮箱已被占用");
    }

    @Test
    void fallsBackToDefaultMessageForPasswordFormat() {
        BizException ex = new BizException(
                "USER_PASSWORD_FORMAT_INVALID",
                "密码长度为 8-32 个字符，支持大小写字母、数字、特殊字符，至少包含字母和数字");
        assertThat(ex.getMessage()).startsWith("密码长度为 8-32 个字符");
    }

    @Test
    void usesCodeWhenNoDefaultMessage() {
        BizException ex = new BizException("SOME_CODE", (String) null);
        assertThat(ex.getMessage()).isEqualTo("SOME_CODE");
    }

    @Test
    void usesDefaultMessageWhenCodeIsNull() {
        BizException ex = new BizException("直接文案");
        assertThat(ex.getMessage()).isEqualTo("直接文案");
    }
}
