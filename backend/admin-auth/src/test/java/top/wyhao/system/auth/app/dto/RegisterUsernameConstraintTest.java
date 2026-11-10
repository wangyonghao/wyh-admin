package top.wyhao.system.auth.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 注册收集邮箱。登录用户名仍不套写入长度。
 */
class RegisterUsernameConstraintTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void openValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void registerEmailRejectsBlankAndAcceptsANormalAddress() {
        assertThat(emailMessages(null)).anyMatch(message -> message.contains("不能为空"));
        assertThat(emailMessages("")).anyMatch(message -> message.contains("不能为空"));
        assertThat(emailMessages("   ")).anyMatch(message -> message.contains("不能为空") || message.contains("格式"));
        assertThat(emailMessages("not-an-email")).anyMatch(message -> message.contains("格式"));
        assertThat(emailMessages("user@example.com")).isEmpty();
        assertThat(emailMessages("a".repeat(60) + "@example.com")).anyMatch(message -> message.contains("64"));
    }

    @Test
    void registerRequestHasNoUsernameField() {
        for (Field field : RegisterRequest.class.getDeclaredFields()) {
            assertThat(field.getName()).isNotEqualTo("username");
        }
    }

    @Test
    void loginUsernameHasNoWritePattern() throws Exception {
        Field username = LoginRequest.class.getDeclaredField("username");
        for (Annotation annotation : username.getAnnotations()) {
            assertThat(annotation.annotationType().getSimpleName()).isNotEqualTo("Pattern");
        }
        LoginRequest login = new LoginRequest();
        login.setUsername("abc");
        assertThat(validator.validateProperty(login, "username")).isEmpty();
    }

    private static Set<String> emailMessages(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("placeholder");
        return validator.validateProperty(request, "email").stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
