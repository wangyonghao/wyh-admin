package top.wyhao.identity.client;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户凭证契约 DTO（无领域行为；供 Auth 等消费者映射为领域对象）。
 */
public record UserCredential(
        Long id,
        String username,
        String displayName,
        String password,
        Integer status,
        Integer gender,
        Long departmentId,
        LocalDateTime pwdUpdateTime,
        LocalDate pwdExpireDate
) {
}
