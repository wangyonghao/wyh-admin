package top.wyhao.system.admin.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import top.wyhao.system.admin.app.dto.UserRequest.Create;
import top.wyhao.system.admin.app.dto.UserRequest.Update;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户名写入注解：新增/导入/租户管理员使用 4-64；修改用户时不重写用户名。
 * <p>
 * 使用 Validator，不起 Spring、不写库。
 */
class UsernameWriteConstraintTest {

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
    void createUserChecksLengthAndBlank() {
        assertThat(messages(new UserRequest(), "username", Create.class)).anyMatch(this::blank);
        assertThat(messages(user(""), Create.class)).anyMatch(this::blank);
        assertThat(messages(user("   "), Create.class)).anyMatch(this::blank);
        assertThat(messages(user(sample(3)), Create.class)).anyMatch(this::length);
        assertThat(messages(user(sample(4)), Create.class)).isEmpty();
        assertThat(messages(user(sample(64)), Create.class)).isEmpty();
        assertThat(messages(user(sample(65)), Create.class)).anyMatch(this::length);
        assertThat(messages(user(null), Create.class)).noneMatch(this::length);
    }

    @Test
    void updateUserDoesNotApplyWriteLengthToOmittedUsername() {
        UserRequest omitted = new UserRequest();
        assertThat(messages(omitted, "username", Update.class)).isEmpty();

        UserRequest present = user(sample(3));
        assertThat(messages(present, "username", Update.class)).anyMatch(message -> message.contains("不能修改用户名"));
    }

    @Test
    void importAndTenantAdminUseTheSameLength() {
        assertThat(messages(importRow(null))).anyMatch(this::blank);
        assertThat(messages(importRow(""))).anyMatch(this::blank);
        assertThat(messages(importRow("   "))).anyMatch(this::blank);
        assertThat(messages(importRow(sample(3)))).anyMatch(this::length);
        assertThat(messages(importRow(sample(4)))).isEmpty();
        assertThat(messages(importRow(sample(64)))).isEmpty();
        assertThat(messages(importRow(sample(65)))).anyMatch(this::length);

        assertThat(messages(tenant(null), TenantRequest.Create.class)).anyMatch(this::blank);
        assertThat(messages(tenant(""), TenantRequest.Create.class)).anyMatch(this::blank);
        assertThat(messages(tenant("   "), TenantRequest.Create.class)).anyMatch(this::blank);
        assertThat(messages(tenant(sample(3)), TenantRequest.Create.class)).anyMatch(this::length);
        assertThat(messages(tenant(sample(4)), TenantRequest.Create.class)).isEmpty();
        assertThat(messages(tenant(sample(64)), TenantRequest.Create.class)).isEmpty();
        assertThat(messages(tenant(sample(65)), TenantRequest.Create.class)).anyMatch(this::length);
    }

    private boolean blank(String message) {
        return message.contains("不能为空");
    }

    private boolean length(String message) {
        return message.contains("4-64");
    }

    private static Set<String> messages(UserRequest request, String property, Class<?> group) {
        return validator.validateProperty(request, property, group).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    private static Set<String> messages(UserRequest request, Class<?> group) {
        return messages(request, "username", group);
    }

    private static Set<String> messages(UserImportRowReq row) {
        return validator.validateProperty(row, "username").stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    private static Set<String> messages(TenantRequest request, Class<?> group) {
        return validator.validateProperty(request, "adminUsername", group).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    private static UserRequest user(String username) {
        UserRequest request = new UserRequest();
        request.setUsername(username);
        return request;
    }

    private static UserImportRowReq importRow(String username) {
        UserImportRowReq row = new UserImportRowReq();
        row.setUsername(username);
        return row;
    }

    private static TenantRequest tenant(String username) {
        TenantRequest request = new TenantRequest();
        request.setAdminUsername(username);
        return request;
    }

    private static String sample(int length) {
        return "a" + "b".repeat(length - 1);
    }
}
