package top.wyhao.system.auth.app.service;

import org.junit.jupiter.api.Test;
import top.wyhao.system.auth.domain.exception.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationLinkOriginTest {

    @Test
    void acceptsHttpsAndLocalHttpOnly() {
        assertThat(new RegistrationLinkOrigin("https://admin.example.com/").requireBaseUrl())
                .isEqualTo("https://admin.example.com");
        assertThat(new RegistrationLinkOrigin("http://127.0.0.1:5888").requireBaseUrl())
                .isEqualTo("http://127.0.0.1:5888");
        assertThat(new RegistrationLinkOrigin("http://localhost:5888").requireBaseUrl())
                .isEqualTo("http://localhost:5888");
    }

    @Test
    void rejectsHostHeaderStyleOrigins() {
        assertThatThrownBy(() -> new RegistrationLinkOrigin("http://evil.example").requireBaseUrl())
                .isInstanceOf(AuthenticationException.class)
                .satisfies(ex -> assertThat(((AuthenticationException) ex).getCode()).isEqualTo("REGISTER_PUBLIC_URL_INVALID"));
    }
}
