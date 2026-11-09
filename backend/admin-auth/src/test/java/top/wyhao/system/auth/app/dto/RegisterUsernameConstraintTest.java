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
 * 注册用户名写入约束，以及登录请求不套用该长度。
 * <p>
 * 使用 Validator，不起 Spring、不写库。
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
    void registerUsernameLengthAndBlank() {
        assertThat(messages(request(null))).anyMatch(this::blank);
        assertThat(messages(request(""))).anyMatch(this::blank);
        assertThat(messages(request("   "))).anyMatch(this::blank);
        assertThat(messages(request(sample(3)))).anyMatch(this::length);
        assertThat(messages(request(sample(4)))).isEmpty();
        assertThat(messages(request(sample(64)))).isEmpty();
        assertThat(messages(request(sample(65)))).anyMatch(this::length);
    }

    @Test
    void loginUsernameHasNoWritePattern() throws Exception {
        Field username = LoginRequest.class.getDeclaredField("username");
        for (Annotation annotation : username.getAnnotations()) {
            assertThat(annotation.annotationType().getSimpleName()).isNotEqualTo("Pattern");
        }
        LoginRequest login = new LoginRequest();
        login.setUsername(sample(3));
        assertThat(validator.validateProperty(login, "username")).isEmpty();
    }

    private boolean blank(String message) {
        return message.contains("不能为空");
    }

    private boolean length(String message) {
        return message.contains("4-64");
    }

    private static Set<String> messages(RegisterRequest request) {
        return validator.validateProperty(request, "username").stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    private static RegisterRequest request(String username) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        return request;
    }

    private static String sample(int length) {
        return "a" + "b".repeat(length - 1);
    }
}
